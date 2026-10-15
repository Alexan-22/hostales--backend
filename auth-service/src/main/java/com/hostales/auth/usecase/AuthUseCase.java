package com.hostales.auth.usecase;

import com.hostales.auth.bean.dto.LoginDto;
import com.hostales.auth.bean.dto.LoginResponseDto;
import com.hostales.auth.bean.dto.UsuarioSesionDto;

public interface AuthUseCase {

    LoginResponseDto login(LoginDto dto);

    UsuarioSesionDto obtenerSesionActual();
}