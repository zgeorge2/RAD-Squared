/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.bank.ignite;

import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.apps.bank.akka.AccountHolder;
import com.rad2.ignite.common.BaseModelRegistry;
import com.rad2.ignite.common.DModel;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class AccountHolderRegistry extends BaseModelRegistry<AccountHolderRegistry.DAccountHolder> {
    @Override
    protected Class getModelClass() {
        return DAccountHolder.class;
    }

    @Override
    public Class getParentRegistryClass() {
        return BankRegistry.class;
    }

    public String getTransactionIdForMoneyTransfer(String key) {
        return this.apply(key, cc -> cc.getNextTransactionId());
    }

    public void creditRewardPoints(String key, long points) {
        this.apply(key, cc -> cc.creditRewardPoints(points));
    }

    public static class DAccountHolder extends DModel {
        @QuerySqlField
        private long lastTransactionId;
        private long rewardPoints;

        public DAccountHolder(AccountHolderRegistryStateDTO dto) {
            super(dto);
            this.lastTransactionId = dto.getTransactionId();
            this.rewardPoints = dto.getRewardPoints();
        }

        @Override
        public RegistryStateDTO toRegistryStateDTO() {
            return new AccountHolderRegistryStateDTO(this);
        }

        @Override
        public Class getActorClass() {
            return AccountHolder.class;
        }

        @Override
        public String toString() {
            return String.format("%s[TID=%d][PTS=%d]", this.getKey(), getTransactionId(), getRewardPoints());
        }

        synchronized String getNextTransactionId() {
            this.lastTransactionId++;
            return String.format("MT_FROM_%s_%d", this.getName(), this.lastTransactionId);
        }

        long getTransactionId() {
            return this.lastTransactionId;
        }

        long getRewardPoints() {
            return this.rewardPoints;
        }

        DAccountHolder creditRewardPoints(long points) {
            this.rewardPoints += points;
            return this;
        }
    }

    public static class AccountHolderRegistryStateDTO extends RegistryStateDTO {
        public static final String ATTR_REWARD_POINTS_KEY = "REWARD_POINTS_KEY";
        public static final String ATTR_TRANSACTION_ID_KEY = "TRANSACTION_ID_KEY";

        public AccountHolderRegistryStateDTO(String parentKey, String name) {
            super(AccountHolderRegistry.class, parentKey, name);
            this.putAttr(ATTR_REWARD_POINTS_KEY, 0L);
            this.putAttr(ATTR_TRANSACTION_ID_KEY, 0L);
        }

        public AccountHolderRegistryStateDTO(DAccountHolder model) {
            super(AccountHolderRegistry.class, model);
            this.putAttr(ATTR_REWARD_POINTS_KEY, model.getRewardPoints());
            this.putAttr(ATTR_TRANSACTION_ID_KEY, model.getTransactionId());
        }

        @Override
        public DModel toModel() {
            return new DAccountHolder(this);
        }

        long getTransactionId() {
            return (long) this.getAttr(ATTR_TRANSACTION_ID_KEY);
        }

        synchronized long getRewardPoints() {
            return (long) this.getAttr(ATTR_REWARD_POINTS_KEY);
        }
    }
}

