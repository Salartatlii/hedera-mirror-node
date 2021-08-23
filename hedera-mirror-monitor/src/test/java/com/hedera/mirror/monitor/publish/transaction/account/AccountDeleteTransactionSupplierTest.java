package com.hedera.mirror.monitor.publish.transaction.account;

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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.mirror.monitor.publish.transaction.AbstractTransactionSupplierTest;

class AccountDeleteTransactionSupplierTest extends AbstractTransactionSupplierTest {

    @Test
    void createWithMinimumData() {
        AccountDeleteTransactionSupplier accountDeleteTransactionSupplier = new AccountDeleteTransactionSupplier();
        accountDeleteTransactionSupplier.setAccountId(ACCOUNT_ID.toString());
        AccountDeleteTransaction actual = accountDeleteTransactionSupplier.get();

        assertThat(actual)
                .returns(ACCOUNT_ID, AccountDeleteTransaction::getAccountId)
                .returns(MAX_TRANSACTION_FEE_HBAR, AccountDeleteTransaction::getMaxTransactionFee)
                .returns(AccountId.fromString("0.0.2"), AccountDeleteTransaction::getTransferAccountId);
    }

    @Test
    void createWithCustomData() {
        AccountDeleteTransactionSupplier accountDeleteTransactionSupplier = new AccountDeleteTransactionSupplier();
        accountDeleteTransactionSupplier.setAccountId(ACCOUNT_ID.toString());
        accountDeleteTransactionSupplier.setTransferAccountId(ACCOUNT_ID_2.toString());
        accountDeleteTransactionSupplier.setMaxTransactionFee(1);
        AccountDeleteTransaction actual = accountDeleteTransactionSupplier.get();

        assertThat(actual)
                .returns(ACCOUNT_ID, AccountDeleteTransaction::getAccountId)
                .returns(ONE_TINYBAR, AccountDeleteTransaction::getMaxTransactionFee)
                .returns(ACCOUNT_ID_2, AccountDeleteTransaction::getTransferAccountId);
    }
}
