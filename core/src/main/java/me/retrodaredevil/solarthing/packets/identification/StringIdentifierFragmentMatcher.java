package me.retrodaredevil.solarthing.packets.identification;

import me.retrodaredevil.solarthing.annotations.NotNull;
import me.retrodaredevil.solarthing.annotations.Nullable;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class StringIdentifierFragmentMatcher implements IdentifierFragmentMatcher {
	private final Integer fragmentId;
	private final String identifierRepresentation;

	public StringIdentifierFragmentMatcher(@Nullable Integer fragmentId, @NotNull String identifierRepresentation) {
		this.fragmentId = fragmentId;
		requireNonNull(this.identifierRepresentation = identifierRepresentation);
	}

	@Override
	public boolean matches(IdentifierFragment identifierFragment) {
		return Objects.equals(fragmentId, identifierFragment.getFragmentId()) && identifierRepresentation.equals(identifierFragment.getIdentifier().getRepresentation());
	}
}
