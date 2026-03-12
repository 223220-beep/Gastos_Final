package com.gastosapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.gastosapp.data.local.UserEntity;
import com.gastosapp.data.repository.UserRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;
import java.util.concurrent.ExecutionException;

@HiltViewModel
public class AuthViewModel extends ViewModel {
    private final UserRepository repository;
    private final MutableLiveData<UserEntity> currentUser = new MutableLiveData<>();
    private final MutableLiveData<String> authError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registerSuccess = new MutableLiveData<>();

    @Inject
    public AuthViewModel(UserRepository repository) {
        this.repository = repository;
    }

    public LiveData<UserEntity> getCurrentUser() {
        return currentUser;
    }

    public LiveData<String> getAuthError() {
        return authError;
    }

    public LiveData<Boolean> getRegisterSuccess() {
        return registerSuccess;
    }

    public void login(String email, String password) {
        try {
            UserEntity user = repository.login(email, password).get();
            if (user != null) {
                currentUser.postValue(user);
            } else {
                authError.postValue("Credenciales inválidas");
            }
        } catch (Exception e) {
            authError.postValue("Error en el servidor: " + e.getMessage());
        }
    }

    public void register(String name, String email, String password) {
        try {
            UserEntity newUser = new UserEntity(name, email, password);
            boolean success = repository.register(newUser).get();
            if (success) {
                registerSuccess.postValue(true);
            } else {
                authError.postValue("El correo ya está registrado");
            }
        } catch (Exception e) {
            authError.postValue("Error al registrar: " + e.getMessage());
        }
    }
}
