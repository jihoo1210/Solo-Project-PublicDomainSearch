package com.example.backend.service.auth.utility;

import org.springframework.stereotype.Service;

@Service
public class RandomState {

    public String getRandomState() {

        StringBuilder state = new StringBuilder();
        String charSet = "qwertyuiopasdfghjklzxcvbnm";

        for(int i = 0; i < 16; i++) {
            int randomIdx = (int) (Math.random() * charSet.length());
            state.append(charSet.charAt(randomIdx));
        }

        return state.toString();
    }
}
