package de.devwhyqueue.odsgradegetter.ui.login;

import androidx.annotation.Nullable;

import de.devwhyqueue.odsgradegetter.tordownloader.model.TranscriptOfRecords;

/**
 * Authentication result : success (user details) or error message.
 */
class LoginResult {
    @Nullable
    private TranscriptOfRecords success;
    @Nullable
    private String error;

    LoginResult(@Nullable String error) {
        this.error = error;
    }

    LoginResult(@Nullable TranscriptOfRecords success) {
        this.success = success;
    }

    @Nullable
    TranscriptOfRecords getSuccess() {
        return success;
    }

    @Nullable
    String getError() {
        return error;
    }
}