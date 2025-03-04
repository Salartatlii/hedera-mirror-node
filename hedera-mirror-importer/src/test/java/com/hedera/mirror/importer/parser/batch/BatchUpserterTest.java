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

import static com.hedera.mirror.common.domain.entity.EntityType.ACCOUNT;
import static com.hedera.mirror.common.domain.entity.EntityType.TOKEN;
import static com.hedera.mirror.importer.config.MirrorImporterConfiguration.TOKEN_DISSOCIATE_BATCH_PERSISTER;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.ByteString;
import com.hederahashgraph.api.proto.java.Key;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Hex;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.transaction.support.TransactionOperations;

import com.hedera.mirror.common.domain.contract.Contract;
import com.hedera.mirror.common.domain.entity.Entity;
import com.hedera.mirror.common.domain.entity.EntityId;
import com.hedera.mirror.common.domain.entity.EntityType;
import com.hedera.mirror.common.domain.schedule.Schedule;
import com.hedera.mirror.importer.IntegrationTest;
import com.hedera.mirror.importer.domain.DomainBuilder;
import com.hedera.mirror.common.domain.token.Nft;
import com.hedera.mirror.common.domain.token.NftId;
import com.hedera.mirror.common.domain.token.NftTransfer;
import com.hedera.mirror.common.domain.token.NftTransferId;
import com.hedera.mirror.common.domain.token.Token;
import com.hedera.mirror.common.domain.token.TokenAccount;
import com.hedera.mirror.common.domain.token.TokenFreezeStatusEnum;
import com.hedera.mirror.common.domain.token.TokenId;
import com.hedera.mirror.common.domain.token.TokenKycStatusEnum;
import com.hedera.mirror.common.domain.token.TokenPauseStatusEnum;
import com.hedera.mirror.common.domain.token.TokenSupplyTypeEnum;
import com.hedera.mirror.common.domain.token.TokenTransfer;
import com.hedera.mirror.common.domain.token.TokenTypeEnum;
import com.hedera.mirror.importer.repository.ContractRepository;
import com.hedera.mirror.importer.repository.EntityRepository;
import com.hedera.mirror.importer.repository.NftRepository;
import com.hedera.mirror.importer.repository.NftTransferRepository;
import com.hedera.mirror.importer.repository.ScheduleRepository;
import com.hedera.mirror.importer.repository.TokenAccountRepository;
import com.hedera.mirror.importer.repository.TokenRepository;
import com.hedera.mirror.importer.repository.TokenTransferRepository;

class BatchUpserterTest extends IntegrationTest {

    private static final Key KEY = Key.newBuilder()
            .setEd25519(ByteString.copyFromUtf8("0a2212200aa8e21064c61eab86e2a9c164565b4e7a9a4146106e0a6cd03a8c"))
            .build();

    @Resource
    private BatchPersister batchPersister;

    @Resource
    private ContractRepository contractRepository;

    @Resource
    private DomainBuilder domainBuilder;

    @Resource
    private EntityRepository entityRepository;

    @Resource
    private JdbcOperations jdbcOperations;

    @Resource
    private NftRepository nftRepository;

    @Resource
    private NftTransferRepository nftTransferRepository;

    @Resource
    private ScheduleRepository scheduleRepository;

    @Resource
    private TokenRepository tokenRepository;

    @Resource
    private TokenAccountRepository tokenAccountRepository;

    @Resource(name = TOKEN_DISSOCIATE_BATCH_PERSISTER)
    private BatchPersister tokenDissociateTransferBatchUpserter;

    @Resource
    private TokenTransferRepository tokenTransferRepository;

    @Resource
    private TransactionOperations transactionOperations;

    @Test
    void contract() {
        Contract contract1 = domainBuilder.contract().get();
        Contract contract2 = domainBuilder.contract().get();
        Contract contract3 = domainBuilder.contract().get();
        var contracts = List.of(contract1, contract2, contract3);

        persist(batchPersister, contracts);
        assertThat(contractRepository.findAll()).containsExactlyInAnyOrderElementsOf(contracts);

        String contract2Memo = contract2.getMemo();
        contract1.setCreatedTimestamp(null);
        contract1.setMemo("updated1");
        contract2.setCreatedTimestamp(null);
        contract2.setMemo(null);
        contract3.setCreatedTimestamp(null);
        contract3.setMemo("");

        persist(batchPersister, contracts);

        assertThat(contractRepository.findAll())
                .hasSize(3)
                .extracting(Contract::getMemo)
                .containsExactlyInAnyOrder(contract1.getMemo(), contract2Memo, contract3.getMemo());
        assertThat(findHistory(Contract.class))
                .hasSize(3)
                .extracting(Contract::getId)
                .containsExactlyInAnyOrder(contract1.getId(), contract2.getId(), contract3.getId());
    }

    @Test
    void entityInsertOnly() {
        var entities = new ArrayList<Entity>();
        long consensusTimestamp = 1;
        entities.add(getEntity(1, consensusTimestamp, consensusTimestamp, "memo-1"));
        entities.add(getEntity(2, consensusTimestamp, consensusTimestamp, null));
        entities.add(getEntity(3, consensusTimestamp, consensusTimestamp, "memo-3"));
        entities.add(getEntity(4, consensusTimestamp, consensusTimestamp, ""));

        persist(batchPersister, entities);
        entities.get(1).setMemo("");
        assertThat(entityRepository.findAll()).containsExactlyInAnyOrderElementsOf(entities);
        assertThat(findHistory(Entity.class)).isEmpty();
    }

    @Test
    void entityInsertAndUpdate() {
        var entities = new ArrayList<Entity>();
        long consensusTimestamp = 1;
        entities.add(getEntity(1, consensusTimestamp, consensusTimestamp, "memo-1"));
        entities.add(getEntity(2, consensusTimestamp, consensusTimestamp, "memo-2"));
        entities.add(getEntity(3, consensusTimestamp, consensusTimestamp, "memo-3"));
        entities.add(getEntity(4, consensusTimestamp, consensusTimestamp, "memo-4"));

        persist(batchPersister, entities);

        assertThat(entityRepository.findAll()).containsExactlyInAnyOrderElementsOf(entities);

        // update
        var updatedEntities = new ArrayList<Entity>();
        long updateTimestamp = 5;

        // updated
        updatedEntities.add(getEntity(2, null, updateTimestamp, null));
        updatedEntities.add(getEntity(3, null, updateTimestamp, ""));
        updatedEntities.add(getEntity(4, null, updateTimestamp, "updated-memo-4"));

        // new inserts
        updatedEntities.add(getEntity(5, null, updateTimestamp, "memo-5"));
        updatedEntities.add(getEntity(6, null, updateTimestamp, "memo-6"));

        persist(batchPersister, updatedEntities); // copy inserts and updates

        assertThat(entityRepository.findAll())
                .hasSize(6)
                .extracting(Entity::getMemo)
                .containsExactlyInAnyOrder("memo-1", "memo-2", "", "updated-memo-4", "memo-5", "memo-6");
        assertThat(findHistory(Entity.class))
                .hasSize(3)
                .extracting(Entity::getId)
                .containsExactlyInAnyOrder(2L, 3L, 4L);
    }

    @Test
    void entityInsertAndUpdateBatched() {
        var entities = new ArrayList<Entity>();
        long consensusTimestamp = 1;
        entities.add(getEntity(1, consensusTimestamp, consensusTimestamp, "memo-1"));
        entities.add(getEntity(2, consensusTimestamp, consensusTimestamp, "memo-2"));
        entities.add(getEntity(3, consensusTimestamp, consensusTimestamp, "memo-3"));
        entities.add(getEntity(4, consensusTimestamp, consensusTimestamp, "memo-4"));

        // update
        long updateTimestamp = 5;

        // updated
        var updateEntities = new ArrayList<Entity>();
        updateEntities.add(getEntity(3, null, updateTimestamp, ""));
        updateEntities.add(getEntity(4, null, updateTimestamp, "updated-memo-4"));

        // new inserts
        updateEntities.add(getEntity(5, null, updateTimestamp, "memo-5"));
        updateEntities.add(getEntity(6, null, updateTimestamp, "memo-6"));

        persist(batchPersister, entities, updateEntities); // copy inserts and updates

        assertThat(entityRepository
                .findAll())
                .isNotEmpty()
                .hasSize(6)
                .extracting(Entity::getMemo)
                .containsExactlyInAnyOrder("memo-1", "memo-2", "", "updated-memo-4", "memo-5", "memo-6");
        assertThat(findHistory(Entity.class))
                .hasSize(2)
                .extracting(Entity::getId)
                .containsExactlyInAnyOrder(3L, 4L);
    }

    @Test
    void tokenInsertOnly() {
        var tokens = new ArrayList<Token>();
        tokens.add(getToken("0.0.2000", "0.0.1001", 1L));
        tokens.add(getToken("0.0.3000", "0.0.1001", 2L));
        tokens.add(getToken("0.0.4000", "0.0.1001", 3L));
        tokens.add(getToken("0.0.5000", "0.0.1001", 4L));

        persist(batchPersister, tokens);
        assertThat(tokenRepository.findAll()).containsExactlyInAnyOrderElementsOf(tokens);
    }

    @Test
    void tokenInsertAndUpdate() {
        // insert
        var tokens = new ArrayList<Token>();
        tokens.add(getToken("0.0.2000", "0.0.1001", 1L));
        tokens.add(getToken("0.0.3000", "0.0.1002", 2L));
        tokens.add(getToken("0.0.4000", "0.0.1003", 3L));
        tokens.add(getToken("0.0.5000", "0.0.1004", 4L));

        persist(batchPersister, tokens);
        assertThat(tokenRepository.findAll()).containsExactlyInAnyOrderElementsOf(tokens);

        // updates
        tokens.clear();
        tokens.add(getToken("0.0.4000", "0.0.2001", null));
        tokens.add(getToken("0.0.5000", "0.0.2002", null));
        tokens.add(getToken("0.0.6000", "0.0.2005", 5L));
        tokens.add(getToken("0.0.7000", "0.0.2006", 6L));

        persist(batchPersister, tokens);

        assertThat(tokenRepository
                .findAll())
                .isNotEmpty()
                .hasSize(6)
                .extracting(Token::getTreasuryAccountId)
                .extracting(EntityId::entityIdToString)
                .containsExactlyInAnyOrder("0.0.1001", "0.0.1002", "0.0.2001", "0.0.2002", "0.0.2005", "0.0.2006");
    }

    @Test
    void tokenUpdateNegativeTotalSupply() {
        // given
        Token token = getToken("0.0.2000", "0.0.1001", 3L);
        tokenRepository.save(token);

        // when
        Token update = new Token();
        update.setModifiedTimestamp(8L);
        update.setTokenId(token.getTokenId());
        update.setTotalSupply(-50L);
        persist(batchPersister, List.of(update));

        // then
        token.setTotalSupply(token.getTotalSupply() - 50L);
        token.setModifiedTimestamp(8L);
        assertThat(tokenRepository.findAll()).containsOnly(token);
    }

    @Test
    void tokenAccountInsertOnly() {
        // inserts token first
        var tokens = new ArrayList<Token>();
        tokens.add(getToken("0.0.2000", "0.0.1001", 1L));
        tokens.add(getToken("0.0.2001", "0.0.1001", 2L));
        tokens.add(getToken("0.0.3000", "0.0.1001", 3L));

        persist(batchPersister, tokens);
        assertThat(tokenRepository.findAll()).containsExactlyInAnyOrderElementsOf(tokens);

        var tokenAccounts = new ArrayList<TokenAccount>();
        tokenAccounts.add(getTokenAccount("0.0.2000", "0.0.1001", 1L, true, 1L));
        tokenAccounts.add(getTokenAccount("0.0.2001", "0.0.1001", 2L, true, 2L));
        tokenAccounts.add(getTokenAccount("0.0.3000", "0.0.4001", 3L, true, 3L));
        tokenAccounts.add(getTokenAccount("0.0.3000", "0.0.4002", 4L, true, 4L));

        persist(batchPersister, tokenAccounts);

        assertThat(tokenRepository.findAll()).containsExactlyInAnyOrderElementsOf(tokens);
        assertThat(tokenAccountRepository
                .findAll())
                .isNotEmpty()
                .extracting(TokenAccount::getCreatedTimestamp, ta -> ta.getId().getModifiedTimestamp())
                .containsExactlyInAnyOrder(
                        Tuple.tuple(1L, 1L),
                        Tuple.tuple(2L, 2L),
                        Tuple.tuple(3L, 3L),
                        Tuple.tuple(4L, 4L)
                );
    }

    @Test
    void tokenAccountInsertFreezeStatus() {
        // inserts token first
        var tokens = new ArrayList<Token>();
        tokens.add(getToken("0.0.2000", "0.0.1001", 1L, false, null, null, null));
        tokens.add(getToken("0.0.3000", "0.0.1001", 2L, true, KEY, null, null));
        tokens.add(getToken("0.0.4000", "0.0.1001", 3L, false, KEY, null, null));

        persist(batchPersister, tokens);
        assertThat(tokenRepository.findAll()).containsExactlyInAnyOrderElementsOf(tokens);

        // associate
        var tokenAccounts = new ArrayList<TokenAccount>();
        tokenAccounts.add(getTokenAccount("0.0.2000", "0.0.2001", 5L, true, 5L));
        tokenAccounts.add(getTokenAccount("0.0.3000", "0.0.3001", 6L, true, 6L));
        tokenAccounts.add(getTokenAccount("0.0.4000", "0.0.4001", 7L, true, 7L));

        persist(batchPersister, tokenAccounts);

        assertThat(tokenRepository.findAll()).containsExactlyInAnyOrderElementsOf(tokens);
        assertThat(tokenAccountRepository.findAll())
                .extracting(ta -> ta.getId().getModifiedTimestamp(), TokenAccount::getFreezeStatus)
                .containsExactlyInAnyOrder(
                        Tuple.tuple(5L, TokenFreezeStatusEnum.NOT_APPLICABLE),
                        Tuple.tuple(6L, TokenFreezeStatusEnum.FROZEN),
                        Tuple.tuple(7L, TokenFreezeStatusEnum.UNFROZEN)
                );

        // reverse freeze status
        tokenAccounts.clear();
        tokenAccounts.add(getTokenAccount("0.0.3000", "0.0.3001", null, null, 10L,
                TokenFreezeStatusEnum.UNFROZEN, null));
        tokenAccounts.add(getTokenAccount("0.0.4000", "0.0.4001", null, null, 11L,
                TokenFreezeStatusEnum.FROZEN, null));

        persist(batchPersister, tokenAccounts);

        assertThat(tokenAccountRepository.findAll())
                .extracting(ta -> ta.getId().getModifiedTimestamp(), TokenAccount::getFreezeStatus)
                .containsExactlyInAnyOrder(
                        Tuple.tuple(5L, TokenFreezeStatusEnum.NOT_APPLICABLE),
                        Tuple.tuple(6L, TokenFreezeStatusEnum.FROZEN),
                        Tuple.tuple(7L, TokenFreezeStatusEnum.UNFROZEN),
                        Tuple.tuple(10L, TokenFreezeStatusEnum.UNFROZEN),
                        Tuple.tuple(11L, TokenFreezeStatusEnum.FROZEN)
                );
    }

    @Test
    void tokenAccountInsertKycStatus() {
        // inserts token first
        var tokens = new ArrayList<Token>();
        tokens.add(getToken("0.0.2000", "0.0.1001", 1L, false, null, null, null));
        tokens.add(getToken("0.0.3000", "0.0.1001", 2L, false, null, KEY, null));

        persist(batchPersister, tokens);
        assertThat(tokenRepository.findAll()).containsExactlyInAnyOrderElementsOf(tokens);

        var tokenAccounts = new ArrayList<TokenAccount>();
        tokenAccounts.add(getTokenAccount("0.0.2000", "0.0.2001", 5L, true, 5L));
        tokenAccounts.add(getTokenAccount("0.0.3000", "0.0.3001", 6L, true, 6L));

        persist(batchPersister, tokenAccounts);

        assertThat(tokenRepository.findAll()).containsExactlyInAnyOrderElementsOf(tokens);
        assertThat(tokenAccountRepository.findAll())
                .extracting(ta -> ta.getId().getModifiedTimestamp(), TokenAccount::getKycStatus)
                .containsExactlyInAnyOrder(
                        Tuple.tuple(5L, TokenKycStatusEnum.NOT_APPLICABLE),
                        Tuple.tuple(6L, TokenKycStatusEnum.REVOKED)
                );

        // grant KYC
        tokenAccounts.clear();
        tokenAccounts.add(getTokenAccount("0.0.3000", "0.0.3001", null, null, 11L,
                null, TokenKycStatusEnum.GRANTED));

        persist(batchPersister, tokenAccounts);

        assertThat(tokenAccountRepository.findAll())
                .extracting(ta -> ta.getId().getModifiedTimestamp(), TokenAccount::getKycStatus)
                .containsExactlyInAnyOrder(
                        Tuple.tuple(5L, TokenKycStatusEnum.NOT_APPLICABLE),
                        Tuple.tuple(6L, TokenKycStatusEnum.REVOKED),
                        Tuple.tuple(11L, TokenKycStatusEnum.GRANTED)
                );
    }

    @Test
    void tokenAccountInsertWithMissingToken() {
        var tokenAccounts = new ArrayList<TokenAccount>();
        tokenAccounts.add(getTokenAccount("0.0.2000", "0.0.1001", 1L, false, 1L));
        tokenAccounts.add(getTokenAccount("0.0.2001", "0.0.1001", 2L, false, 2L));
        tokenAccounts.add(getTokenAccount("0.0.3000", "0.0.4001", 3L, false, 3L));
        tokenAccounts.add(getTokenAccount("0.0.3000", "0.0.4002", 4L, false, 4L));

        persist(batchPersister, tokenAccounts);
        assertThat(tokenAccountRepository.findAll()).isEmpty();
    }

    @Test
    void tokenAccountInsertAndUpdate() {
        // inserts token first
        var tokens = new ArrayList<Token>();
        tokens.add(getToken("0.0.2000", "0.0.1001", 1L));
        tokens.add(getToken("0.0.2001", "0.0.1001", 2L));
        tokens.add(getToken("0.0.3000", "0.0.1001", 3L));
        tokens.add(getToken("0.0.4000", "0.0.1001", 4L));

        persist(batchPersister, tokens);
        assertThat(tokenRepository.findAll()).containsExactlyInAnyOrderElementsOf(tokens);

        var tokenAccounts = new ArrayList<TokenAccount>();
        tokenAccounts.add(getTokenAccount("0.0.2000", "0.0.1001", 5L, true, 6L));
        tokenAccounts.add(getTokenAccount("0.0.2001", "0.0.1001", 6L, true, 7L));
        tokenAccounts.add(getTokenAccount("0.0.3000", "0.0.4001", 7L, true, 8L));
        tokenAccounts.add(getTokenAccount("0.0.3000", "0.0.4002", 8L, true, 9L));

        persist(batchPersister, tokenAccounts);

        // update
        tokenAccounts.clear();
        tokenAccounts.add(getTokenAccount("0.0.3000", "0.0.4001", null, null, 10L));
        tokenAccounts.add(getTokenAccount("0.0.3000", "0.0.4002", null, null, 11L));
        tokenAccounts.add(getTokenAccount("0.0.4000", "0.0.7001", 10L, true, 12L));
        tokenAccounts.add(getTokenAccount("0.0.4000", "0.0.7002", 11L, true, 13L));

        persist(batchPersister, tokenAccounts);

        assertThat(tokenAccountRepository.findAll())
                .extracting(TokenAccount::getCreatedTimestamp, ta -> ta.getId().getModifiedTimestamp())
                .containsExactlyInAnyOrder(
                        Tuple.tuple(5L, 6L),
                        Tuple.tuple(6L, 7L),
                        Tuple.tuple(7L, 8L),
                        Tuple.tuple(7L, 10L),
                        Tuple.tuple(8L, 9L),
                        Tuple.tuple(8L, 11L),
                        Tuple.tuple(10L, 12L),
                        Tuple.tuple(11L, 13L)
                );
    }

    @Test
    void scheduleInsertOnly() {
        var schedules = new ArrayList<Schedule>();
        schedules.add(getSchedule(1L, "0.0.1001", null));
        schedules.add(getSchedule(2L, "0.0.1002", null));
        schedules.add(getSchedule(3L, "0.0.1003", null));
        schedules.add(getSchedule(4L, "0.0.1004", null));

        persist(batchPersister, schedules);
        assertThat(scheduleRepository.findAll()).containsExactlyInAnyOrderElementsOf(schedules);
    }

    @Test
    void scheduleInsertAndUpdate() {
        var schedules = new ArrayList<Schedule>();
        schedules.add(getSchedule(1L, "0.0.1001", null));
        schedules.add(getSchedule(2L, "0.0.1002", null));
        schedules.add(getSchedule(3L, "0.0.1003", null));
        schedules.add(getSchedule(4L, "0.0.1004", null));

        persist(batchPersister, schedules);
        assertThat(scheduleRepository.findAll()).containsExactlyInAnyOrderElementsOf(schedules);

        // update
        schedules.clear();

        schedules.add(getSchedule(null, "0.0.1003", 5L));
        schedules.add(getSchedule(null, "0.0.1004", 6L));
        schedules.add(getSchedule(7L, "0.0.1005", null));
        schedules.add(getSchedule(8L, "0.0.1006", null));

        persist(batchPersister, schedules);
        assertThat(scheduleRepository
                .findAll())
                .isNotEmpty()
                .hasSize(6)
                .extracting(Schedule::getExecutedTimestamp)
                .containsExactlyInAnyOrder(null, null, 5L, 6L, null, null);
    }

    @Test
    void nftInsertMissingToken() {
        // mint
        var nfts = new ArrayList<Nft>();
        nfts.add(getNft("0.0.2000", 1, null, 1L, 1L, "nft1", false));
        nfts.add(getNft("0.0.3000", 2, null, 2L, 2L, "nft2", false));
        nfts.add(getNft("0.0.4000", 3, null, 3L, 3L, "nft3", false));
        nfts.add(getNft("0.0.5000", 4, null, 4L, 4L, "nft4", false));

        persist(batchPersister, nfts);
        assertThat(nftRepository.findAll()).isEmpty();
    }

    @Test
    void nftMint() {
        // inserts tokens first
        var tokens = new ArrayList<Token>();
        tokens.add(getToken("0.0.2000", "0.0.98", 1L));
        tokens.add(getToken("0.0.3000", "0.0.98", 2L));
        tokens.add(getToken("0.0.4000", "0.0.98", 3L));
        tokens.add(getToken("0.0.5000", "0.0.98", 4L));

        persist(batchPersister, tokens);
        assertThat(tokenRepository.findAll()).containsExactlyInAnyOrderElementsOf(tokens);

        // insert due to mint
        var nfts = new ArrayList<Nft>();
        nfts.add(getNft("0.0.2000", 1, "0.0.1001", 10L, 10L, "nft1", false));
        nfts.add(getNft("0.0.3000", 2, "0.0.1002", 11L, 11L, "nft2", false));
        nfts.add(getNft("0.0.4000", 3, "0.0.1003", 12L, 12L, "nft3", false));
        nfts.add(getNft("0.0.5000", 4, "0.0.1004", 13L, 13L, "nft4", false));

        persist(batchPersister, nfts);

        assertThat(nftRepository
                .findAll())
                .isNotEmpty()
                .hasSize(4)
                .extracting(Nft::getAccountId)
                .extracting(EntityId::entityIdToString)
                .containsExactlyInAnyOrder("0.0.1001", "0.0.1002", "0.0.1003", "0.0.1004");
    }

    @Test
    void nftInsertAndUpdate() {
        // inserts tokens first
        var tokens = new ArrayList<Token>();
        tokens.add(getToken("0.0.2000", "0.0.98", 1L));
        tokens.add(getToken("0.0.3000", "0.0.98", 2L));
        tokens.add(getToken("0.0.4000", "0.0.98", 3L));
        tokens.add(getToken("0.0.5000", "0.0.98", 4L));
        tokens.add(getToken("0.0.6000", "0.0.98", 5L));
        tokens.add(getToken("0.0.7000", "0.0.98", 6L));

        persist(batchPersister, tokens);
        assertThat(tokenRepository.findAll()).containsExactlyInAnyOrderElementsOf(tokens);

        // insert due to mint
        var nfts = new ArrayList<Nft>();
        nfts.add(getNft("0.0.2000", 1, "0.0.1001", 10L, 10L, "nft1", false));
        nfts.add(getNft("0.0.3000", 2, "0.0.1002", 11L, 11L, "nft2", false));
        nfts.add(getNft("0.0.4000", 3, "0.0.1003", 12L, 12L, "nft3", false));
        nfts.add(getNft("0.0.5000", 4, "0.0.1004", 13L, 13L, "nft4", false));

        persist(batchPersister, nfts);
        assertThat(nftRepository.findAll()).containsExactlyInAnyOrderElementsOf(nfts);

        // updates with transfer
        nfts.clear();
        nfts.add(getNft("0.0.4000", 3, "0.0.1013", null, 15L, null, null));
        nfts.add(getNft("0.0.5000", 4, "0.0.1014", null, 16L, null, null));
        nfts.add(getNft("0.0.6000", 5, "0.0.1015", 17L, 17L, "nft5", false));
        nfts.add(getNft("0.0.7000", 6, "0.0.1016", 18L, 18L, "nft6", false));

        persist(batchPersister, nfts);

        assertThat(nftRepository
                .findAll())
                .isNotEmpty()
                .hasSize(6)
                .extracting(Nft::getAccountId)
                .extracting(EntityId::entityIdToString)
                .containsExactlyInAnyOrder("0.0.1001", "0.0.1002", "0.0.1013", "0.0.1014", "0.0.1015", "0.0.1016");
    }

    @Test
    void nftInsertTransferBurnWipe() {
        // inserts tokens first
        var tokens = new ArrayList<Token>();
        tokens.add(getToken("0.0.2000", "0.0.98", 1L));
        tokens.add(getToken("0.0.3000", "0.0.98", 2L));
        tokens.add(getToken("0.0.4000", "0.0.98", 3L));
        tokens.add(getToken("0.0.5000", "0.0.98", 4L));

        persist(batchPersister, tokens);
        assertThat(tokenRepository.findAll()).containsExactlyInAnyOrderElementsOf(tokens);

        // insert due to mint
        var nfts = new ArrayList<Nft>();
        nfts.add(getNft("0.0.2000", 1, "0.0.1001", 10L, 10L, "nft1", false));
        nfts.add(getNft("0.0.3000", 2, "0.0.1002", 11L, 11L, "nft2", false));
        nfts.add(getNft("0.0.4000", 3, "0.0.1003", 12L, 12L, "nft3", false));
        nfts.add(getNft("0.0.5000", 4, "0.0.1004", 13L, 13L, "nft4", false));

        persist(batchPersister, nfts);
        assertThat(nftRepository.findAll()).containsExactlyInAnyOrderElementsOf(nfts);

        // updates with mint transfers
        nfts.clear();
        nfts.add(getNft("0.0.2000", 1, "0.0.1005", null, 15L, null, false));
        nfts.add(getNft("0.0.3000", 2, "0.0.1006", null, 16L, null, false));
        nfts.add(getNft("0.0.4000", 3, "0.0.1007", null, 17L, null, false));
        nfts.add(getNft("0.0.5000", 4, "0.0.1008", null, 18L, null, false));

        persist(batchPersister, nfts);
        assertThat(nftRepository
                .findAll())
                .isNotEmpty()
                .hasSize(4)
                .extracting(Nft::getAccountId)
                .extracting(EntityId::entityIdToString)
                .containsExactlyInAnyOrder("0.0.1005", "0.0.1006", "0.0.1007", "0.0.1008");

        // updates with wipe/burn
        nfts.clear();
        nfts.add(getNft("0.0.3000", 2, "0.0.0", null, 21L, null, true));
        nfts.add(getNft("0.0.5000", 4, "0.0.0", null, 23L, null, true));
        persist(batchPersister, nfts);

        assertThat(nftRepository
                .findAll())
                .isNotEmpty()
                .hasSize(4)
                .extracting(Nft::getDeleted)
                .containsExactlyInAnyOrder(false, true, false, true);
    }

    @Test
    void tokenDissociateTransfer() {
        // given
        EntityId accountId = EntityId.of("0.0.215", ACCOUNT);
        EntityId accountId2 = EntityId.of("0.0.216", ACCOUNT);
        Token nftClass1 = getDeletedNftClass(10L, 25L, EntityId.of("0.0.100", TOKEN));
        Token nftClass2 = getDeletedNftClass(11L, 24L, EntityId.of("0.0.101", TOKEN));

        EntityId tokenId1 = nftClass1.getTokenId().getTokenId();
        Nft nft1 = getNft(tokenId1, accountId, 1L, 11L, 16L, true); // already deleted, result of wipe
        Nft nft2 = getNft(tokenId1, accountId, 2L, 11L, 11L, false);
        Nft nft3 = getNft(tokenId1, accountId, 3L, 12L, 12L, false);
        Nft nft4 = getNft(tokenId1, accountId2, 4L, 18L, 18L, false); // different account

        Nft nft5 = getNft(nftClass2.getTokenId().getTokenId(), accountId, 1L, 15L, 15L, false);

        nftRepository.saveAll(List.of(nft1, nft2, nft3, nft4, nft5));
        tokenRepository.saveAll(List.of(nftClass1, nftClass2));

        long consensusTimestamp = 30L;
        EntityId ftId = EntityId.of("0.0.217", TOKEN);
        EntityId payerId = EntityId.of("0.0.2002", ACCOUNT);
        TokenTransfer fungibleTokenTransfer = domainBuilder.tokenTransfer().customize(t -> t
                .amount(-10)
                .id(new TokenTransfer.Id(consensusTimestamp, ftId, accountId))
                .payerAccountId(payerId)
                .tokenDissociate(true)).get();
        TokenTransfer nonFungibleTokenTransfer = domainBuilder.tokenTransfer().customize(t -> t
                .amount(-2)
                .id(new TokenTransfer.Id(consensusTimestamp, tokenId1, accountId))
                .payerAccountId(payerId)
                .tokenDissociate(true)).get();
        List<TokenTransfer> tokenTransfers = List.of(fungibleTokenTransfer, nonFungibleTokenTransfer);

        // when
        persist(tokenDissociateTransferBatchUpserter, tokenTransfers);

        // then
        assertThat(nftRepository.findAll()).containsExactlyInAnyOrder(
                nft1,
                getNft(tokenId1, accountId, 2L, 11L, 30L, true),
                getNft(tokenId1, accountId, 3L, 12L, 30L, true),
                nft4,
                nft5
        );

        NftTransfer serial2Transfer = getNftTransfer(tokenId1, accountId, 2L, consensusTimestamp);
        serial2Transfer.setPayerAccountId(payerId);
        NftTransfer serial3Transfer = getNftTransfer(tokenId1, accountId, 3L, consensusTimestamp);
        serial3Transfer.setPayerAccountId(payerId);
        assertThat(nftTransferRepository.findAll()).containsExactlyInAnyOrder(serial2Transfer, serial3Transfer);

        assertThat(tokenTransferRepository.findAll())
                .usingElementComparatorIgnoringFields("tokenDissociate")
                .containsOnly(fungibleTokenTransfer);
    }

    private void persist(BatchPersister batchPersister, Collection<?>... items) {
        transactionOperations.executeWithoutResult(t -> {
            for (Collection batch : items) {
                batchPersister.persist(batch);
            }
        });
    }

    private <T> Collection<T> findHistory(Class<T> historyClass) {
        String table = historyClass.getSimpleName().toLowerCase();
        String sql = String.format("select * from %s_history order by id asc, timestamp_range asc", table);
        return jdbcOperations.query(sql, rowMapper(historyClass));
    }

    private Entity getEntity(long id, Long createdTimestamp, long modifiedTimestamp, String memo) {
        Entity entity = new Entity();
        entity.setId(id);
        entity.setCreatedTimestamp(createdTimestamp);
        entity.setModifiedTimestamp(modifiedTimestamp);
        entity.setNum(id);
        entity.setRealm(0L);
        entity.setShard(0L);
        entity.setType(ACCOUNT);
        entity.setMemo(memo);
        return entity;
    }

    private Token getToken(String tokenId, String treasuryAccountId, Long createdTimestamp) {
        return getToken(tokenId, treasuryAccountId, createdTimestamp, false, null, null, null);
    }

    @SneakyThrows
    private Token getToken(String tokenId, String treasuryAccountId, Long createdTimestamp,
                           Boolean freezeDefault, Key freezeKey, Key kycKey, Key pauseKey) {
        var instr = "0011223344556677889900aabbccddeeff0011223344556677889900aabbccddeeff";
        var hexKey = Key.newBuilder().setEd25519(ByteString.copyFrom(Hex.decodeHex(instr))).build().toByteArray();
        Token token = new Token();
        token.setCreatedTimestamp(createdTimestamp);
        token.setDecimals(1000);
        token.setFreezeDefault(freezeDefault);
        token.setFreezeKey(freezeKey != null ? freezeKey.toByteArray() : null);
        token.setInitialSupply(1_000_000_000L);
        token.setKycKey(kycKey != null ? kycKey.toByteArray() : null);
        token.setPauseKey(pauseKey != null ? pauseKey.toByteArray() : null);
        token.setPauseStatus(pauseKey != null ? TokenPauseStatusEnum.UNPAUSED : TokenPauseStatusEnum.NOT_APPLICABLE);
        token.setMaxSupply(1_000_000_000L);
        token.setModifiedTimestamp(3L);
        token.setName("FOO COIN TOKEN" + tokenId);
        token.setSupplyKey(hexKey);
        token.setSupplyType(TokenSupplyTypeEnum.INFINITE);
        token.setSymbol("FOOTOK" + tokenId);
        token.setTokenId(new TokenId(EntityId.of(tokenId, TOKEN)));
        token.setTreasuryAccountId(EntityId.of(treasuryAccountId, ACCOUNT));
        token.setType(TokenTypeEnum.FUNGIBLE_COMMON);
        token.setWipeKey(hexKey);
        return token;
    }

    private TokenAccount getTokenAccount(String tokenId, String accountId, Long createdTimestamp, Boolean associated,
                                         Long modifiedTimestamp) {
        return getTokenAccount(tokenId, accountId, createdTimestamp, associated, modifiedTimestamp, null, null);
    }

    private TokenAccount getTokenAccount(String tokenId, String accountId, Long createdTimestamp, Boolean associated,
                                         Long modifiedTimestamp, TokenFreezeStatusEnum freezeStatus,
                                         TokenKycStatusEnum kycStatus) {
        TokenAccount tokenAccount = new TokenAccount(EntityId.of(tokenId, TOKEN),
                EntityId.of(accountId, ACCOUNT), modifiedTimestamp);
        tokenAccount.setAssociated(associated);
        tokenAccount.setAutomaticAssociation(false);
        tokenAccount.setCreatedTimestamp(createdTimestamp);
        tokenAccount.setFreezeStatus(freezeStatus);
        tokenAccount.setKycStatus(kycStatus);

        return tokenAccount;
    }

    private Schedule getSchedule(Long createdTimestamp, String scheduleId, Long executedTimestamp) {
        Schedule schedule = new Schedule();
        schedule.setConsensusTimestamp(createdTimestamp);
        schedule.setCreatorAccountId(EntityId.of("0.0.123", EntityType.ACCOUNT));
        schedule.setExecutedTimestamp(executedTimestamp);
        schedule.setPayerAccountId(EntityId.of("0.0.456", EntityType.ACCOUNT));
        schedule.setScheduleId(EntityId.of(scheduleId, EntityType.SCHEDULE));
        schedule.setTransactionBody("transaction body".getBytes());
        return schedule;
    }

    private Nft getNft(EntityId tokenId, EntityId accountId, long serialNumber, long createdTimestamp,
                       long modifiedTimestamp, boolean deleted) {
        return getNft(tokenId.entityIdToString(), serialNumber, accountId.entityIdToString(), createdTimestamp,
                modifiedTimestamp, "meta", deleted);
    }

    private Nft getNft(String tokenId, long serialNumber, String accountId, Long createdTimestamp,
                       long modifiedTimeStamp, String metadata, Boolean deleted) {
        Nft nft = new Nft();
        nft.setAccountId(accountId == null ? null : EntityId.of(accountId, EntityType.ACCOUNT));
        nft.setCreatedTimestamp(createdTimestamp);
        nft.setDeleted(deleted);
        nft.setId(new NftId(serialNumber, EntityId.of(tokenId, TOKEN)));
        nft.setMetadata(metadata == null ? null : metadata.getBytes(StandardCharsets.UTF_8));
        nft.setModifiedTimestamp(modifiedTimeStamp);
        return nft;
    }

    private NftTransfer getNftTransfer(EntityId tokenId, EntityId senderAccountId, long serialNumber,
                                       long consensusTimestamp) {
        NftTransfer nftTransfer = new NftTransfer();
        nftTransfer.setId(new NftTransferId(consensusTimestamp, serialNumber, tokenId));
        nftTransfer.setSenderAccountId(senderAccountId);
        return nftTransfer;
    }

    private Token getDeletedNftClass(long createdTimestamp, long deletedTimestamp, EntityId tokenId) {
        Token token = Token.of(tokenId);
        token.setCreatedTimestamp(createdTimestamp);
        token.setDecimals(0);
        token.setFreezeDefault(false);
        token.setInitialSupply(0L);
        token.setModifiedTimestamp(deletedTimestamp);
        token.setName("foo");
        token.setPauseStatus(TokenPauseStatusEnum.NOT_APPLICABLE);
        token.setSupplyType(TokenSupplyTypeEnum.FINITE);
        token.setSymbol("bar");
        token.setTotalSupply(200L);
        token.setTreasuryAccountId(EntityId.of("0.0.200", EntityType.ACCOUNT));
        token.setType(TokenTypeEnum.NON_FUNGIBLE_UNIQUE);
        return token;
    }
}
