package de.devwhyqueue.odsgradegetter.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import de.devwhyqueue.odsgradegetter.R;
import de.devwhyqueue.odsgradegetter.tordownloader.TranscriptOfRecordsDownloader;
import de.devwhyqueue.odsgradegetter.tordownloader.model.Credentials;

public class LoginViewModel extends ViewModel {

    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String username, String password) {
        TranscriptOfRecordsDownloader torDownloader = new TranscriptOfRecordsDownloader(new Credentials(username, password));
        torDownloader.start().thenAccept(tor -> loginResult.postValue(new LoginResult(tor))).exceptionally(e -> {
            loginResult.postValue(new LoginResult(e.getCause().getMessage()));
            return null;
        });
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        return !username.trim().isEmpty();
    }

    private boolean isPasswordValid(String password) {
        if (password == null) {
            return false;
        }
        return !password.trim().isEmpty();
    }
}