/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.apps.finco.ignite;

import com.rad2.akka.common.RegistryStateDTO;
import com.rad2.apps.bank.akka.AccountHolder;
import com.rad2.ignite.common.BaseModelRegistry;
import com.rad2.ignite.common.DModel;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class FCAccountHolderRegistry extends BaseModelRegistry<FCAccountHolderRegistry.D_FC_AccountHolder> {
    @Override
    protected Class getModelClass() {
        return D_FC_AccountHolder.class;
    }

    @Override
    public Class getParentRegistryClass() {
        return FinCoRegistry.class;
    }

    public String getTransactionIdForMoneyTransfer(String key) {
        return this.apply(key, D_FC_AccountHolder::getNextTransactionId);
    }

    public void creditRewardPoints(String key, long points) {
        this.apply(key, cc -> cc.creditRewardPoints(points));
    }

    public static class D_FC_AccountHolder extends DModel {
        @QuerySqlField
        private long lastTransactionId;
        private long rewardPoints;

        public D_FC_AccountHolder(FCAccountHolderRegistryStateDTO dto) {
            super(dto);
            this.lastTransactionId = dto.getTransactionId();
            this.rewardPoints = dto.getRewardPoints();
        }

        @Override
        public RegistryStateDTO toRegistryStateDTO() {
            return new FCAccountHolderRegistryStateDTO(this);
        }

        @Override
        public Class getActorClass() {
            return AccountHolder.class;
        }

        synchronized String getNextTransactionId() {
            this.lastTransactionId++;
            return String.format("MT_FROM_%s_%d", this.getName(), this.lastTransactionId);
        }

        long getTransactionId() {
            return this.lastTransactionId;
        }

        public long getRewardPoints() {
            return this.rewardPoints;
        }

        D_FC_AccountHolder creditRewardPoints(long points) {
            this.rewardPoints += points;
            return this;
        }
    }

    public static class FCAccountHolderRegistryStateDTO extends RegistryStateDTO {
        public static final String ATTR_REWARD_POINTS_KEY = "REWARD_POINTS_KEY";
        public static final String ATTR_TRANSACTION_ID_KEY = "TRANSACTION_ID_KEY";

        public FCAccountHolderRegistryStateDTO(String parentKey, String name) {
            super(FCAccountHolderRegistry.class, parentKey, name);
            this.putAttr(ATTR_REWARD_POINTS_KEY, 0L);
            this.putAttr(ATTR_TRANSACTION_ID_KEY, 0L);
        }

        public FCAccountHolderRegistryStateDTO(D_FC_AccountHolder model) {
            super(FCAccountHolderRegistry.class, model);
            this.putAttr(ATTR_REWARD_POINTS_KEY, model.getRewardPoints());
            this.putAttr(ATTR_TRANSACTION_ID_KEY, model.getTransactionId());
        }

        @Override
        public DModel toModel() {
            return new D_FC_AccountHolder(this);
        }

        long getTransactionId() {
            return (long) this.getAttr(ATTR_TRANSACTION_ID_KEY);
        }

        synchronized long getRewardPoints() {
            return (long) this.getAttr(ATTR_REWARD_POINTS_KEY);
        }
    }
}

