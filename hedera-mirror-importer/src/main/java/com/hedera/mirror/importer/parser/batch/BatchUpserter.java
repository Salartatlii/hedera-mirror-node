package com.hedera.mirror.importer.parser.batch;

/*-
 * ‌
 * Hedera Mirror Node
 * ​
 * Copyright (C) 2019 - 2021 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */

import com.google.common.base.Stopwatch;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import javax.sql.DataSource;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.hedera.mirror.importer.exception.ParserException;
import com.hedera.mirror.importer.parser.CommonParserProperties;
import com.hedera.mirror.importer.repository.upsert.UpsertQueryGenerator;

/**
 * Stateless writer to upsert rows into PostgreSQL using COPY into a temp table then insert and update into final table
 */
@Log4j2
public class BatchUpserter extends BatchInserter {

    private static final String TABLE = "table";

    private final String createTempTableSql;
    private final String createTempIndexSql;
    private final String finalTableName;
    private final String insertSql;
    private final String setTempBuffersSql;
    private final String updateSql;
    private final Timer copyDurationMetric;
    private final Timer finalInsertDurationMetric;
    private final Timer updateDurationMetric;
    private final String truncateSql;

    public BatchUpserter(Class<?> entityClass, DataSource dataSource, MeterRegistry meterRegistry,
                         CommonParserProperties properties,
                         UpsertQueryGenerator upsertQueryGenerator) {
        super(entityClass, dataSource, meterRegistry, properties, upsertQueryGenerator.getTemporaryTableName());
        createTempIndexSql = upsertQueryGenerator.getCreateTempIndexQuery();
        createTempTableSql = upsertQueryGenerator.getCreateTempTableQuery();
        setTempBuffersSql = String.format("set temp_buffers = '%dMB'", properties.getTempTableBufferSize());
        truncateSql = String
                .format("truncate table %s restart identity cascade", upsertQueryGenerator.getTemporaryTableName());
        finalTableName = upsertQueryGenerator.getFinalTableName();
        insertSql = upsertQueryGenerator.getInsertQuery();
        updateSql = upsertQueryGenerator.getUpdateQuery();
        copyDurationMetric = Timer.builder("hedera.mirror.importer.parse.upsert.copy")
                .description("Time to copy transaction information from importer to temp table")
                .tag(TABLE, upsertQueryGenerator.getTemporaryTableName())
                .register(meterRegistry);
        finalInsertDurationMetric = Timer.builder("hedera.mirror.importer.parse.upsert.insert")
                .description("Time to insert transaction information from temp to final table")
                .tag(TABLE, finalTableName)
                .register(meterRegistry);
        if (StringUtils.isNotEmpty(updateSql)) {
            updateDurationMetric = Timer.builder("hedera.mirror.importer.parse.upsert.update")
                    .description("Time to update parsed transactions information into table")
                    .tag(TABLE, finalTableName)
                    .register(meterRegistry);
        } else {
            updateDurationMetric = null;
        }
    }

    @Override
    protected void persistItems(Collection<?> items, Connection connection) {
        if (CollectionUtils.isEmpty(items)) {
            return;
        }

        try {
            // create temp table to copy into
            createTempTable(connection);

            // copy items to temp table
            copyItems(items, connection);

            // insert items from temp table to final table
            int insertCount = insertItems(connection);

            // update items in final table from temp table
            int updateCount = updateItems(connection);

            log.debug("Inserted {} and updated {} from a total of {} rows to {}",
                    insertCount, updateCount, items.size(), finalTableName);
        } catch (Exception e) {
            throw new ParserException(String.format("Error copying %d items to table %s", items.size(), tableName), e);
        }
    }

    private int insertToFinalTable(Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
            int count = preparedStatement.executeUpdate();
            log.trace("Inserted {} rows from {} table to {} table", count, tableName, finalTableName);
            return count;
        }
    }

    private int updateFinalTable(Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(updateSql)) {
            int count = preparedStatement.executeUpdate();
            log.trace("Updated {} rows from {} table to {} table", count, tableName, finalTableName);
            return count;
        }
    }

    private void createTempTable(Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(setTempBuffersSql)) {
            preparedStatement.executeUpdate();
        }

        // create temporary table without constraints to allow for upsert logic to determine missing data vs nulls
        try (PreparedStatement preparedStatement = connection.prepareStatement(createTempTableSql)) {
            preparedStatement.executeUpdate();
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(createTempIndexSql)) {
            preparedStatement.executeUpdate();
        }

        // ensure table is empty in case of batching
        try (PreparedStatement preparedStatement = connection.prepareStatement(truncateSql)) {
            preparedStatement.executeUpdate();
        }
        log.trace("Created temp table {}", tableName);
    }

    private void copyItems(Collection<?> items, Connection connection) throws SQLException, IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        super.persistItems(items, connection);
        recordMetric(copyDurationMetric, stopwatch);
    }

    private int insertItems(Connection connection) throws SQLException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        int insertCount = insertToFinalTable(connection);
        recordMetric(finalInsertDurationMetric, stopwatch);
        return insertCount;
    }

    private int updateItems(Connection connection) throws SQLException {
        if (StringUtils.isEmpty(updateSql)) {
            return 0;
        }

        Stopwatch stopwatch = Stopwatch.createStarted();
        int count = updateFinalTable(connection);
        recordMetric(updateDurationMetric, stopwatch);
        return count;
    }

    private void recordMetric(Timer timer, Stopwatch stopwatch) {
        timer.record(stopwatch.elapsed());
    }
}

