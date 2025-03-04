package com.github.torleifg.bookquest.application.domain;

import java.util.List;

public record Contributor(List<Role> roles, String name) {
}
