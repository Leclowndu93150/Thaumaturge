package com.leclowndu93150.thaumaturge.api.aura;

import java.util.UUID;

/** Implemented by a loaded machine block entity. Both identities must survive saving. */
public interface IAuraBaselineMachine {
    UUID auraMachineIdentity();

    UUID auraMachineOwner();
}
