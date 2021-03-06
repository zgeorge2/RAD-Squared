/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.akka.router;

/**
 * A WorkerActor is a marker interface that establishes the contract, conventionally, that the implementing
 * class will have a constructor that takes a WorkerClassArgs instance in order to construct that Routee
 * (WorkerActor) actor.
 */
public interface WorkerActor {
}
