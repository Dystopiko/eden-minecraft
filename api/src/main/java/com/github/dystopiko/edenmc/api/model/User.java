package com.github.dystopiko.edenmc.api.model;

import com.github.dystopiko.edenmc.api.model.organization.MemberRank;
import org.checkerframework.checker.nullness.qual.NonNull;

@SuppressWarnings("unused")
public interface User {
    boolean isBedrock();

    @NonNull
    MemberRank getMemberRank();
}
