package com.wallhack.clouddrive.authentication.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthDTO(@NotBlank(message = "Username is required")
                      @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
                      @JsonProperty("username") String username,
                      @NotBlank(message = "Password is required")
                      @JsonProperty("password") String password) {

    public JsonObject convertToJSON() {
        return Json.createObjectBuilder()
                .add("username", username())
                .add("password", password())
                .build();
    }
}
