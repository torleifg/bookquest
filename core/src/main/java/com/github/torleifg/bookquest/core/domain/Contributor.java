package com.github.torleifg.bookquest.core.domain;

import java.util.List;

public record Contributor(List<Role> roles, String name) {
}
