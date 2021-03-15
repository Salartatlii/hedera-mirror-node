package com.hedera.datagenerator.sdk.supplier.consensus;

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

import javax.validation.constraints.Min;
import lombok.Data;

import com.hedera.datagenerator.common.Utility;
import com.hedera.datagenerator.sdk.supplier.TransactionSupplier;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.TopicCreateTransaction;

@Data
public class ConsensusCreateTopicTransactionSupplier implements TransactionSupplier<TopicCreateTransaction> {

    private String adminKey;

    private String autoRenewAccountId;

    @Min(1)
    private long maxTransactionFee = 1_000_000_000;

    @Override
    public TopicCreateTransaction get() {
        String memo = Utility.getMemo("Mirror node created test topic");
        TopicCreateTransaction topicCreateTransaction = new TopicCreateTransaction()
                .setMaxTransactionFee(Hbar.fromTinybars(maxTransactionFee))
                .setTopicMemo(memo)
                .setTransactionMemo(memo);

        if (adminKey != null) {
            PublicKey key = PublicKey.fromString(adminKey);
            topicCreateTransaction
                    .setAdminKey(key)
                    .setSubmitKey(key);
        }
        if (autoRenewAccountId != null) {
            topicCreateTransaction
                    .setAutoRenewAccountId(AccountId.fromString(autoRenewAccountId));
        }
        return topicCreateTransaction;
    }
}
