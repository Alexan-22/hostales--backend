package com.hostales.auth.controller;

import com.hostales.auth.bean.dto.*;
import com.hostales.auth.usecase.AuthUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthUseCase authUseCase;

    @PostMapping("/login")
    public LoginResponseDto login(
            @Valid @RequestBody LoginDto dto
    ) {
        return authUseCase.login(dto);
    }

    @GetMapping("/me")
    public UsuarioSesionDto sesionActual() {
        return authUseCase.obtenerSesionActual();
    }
}
