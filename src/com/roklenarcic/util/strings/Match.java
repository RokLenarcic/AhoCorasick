package com.roklenarcic.util.strings;

final class Match {
	Match subMatch;
	final String word;

	Match(final String word) {
		this.word = word;
	}

	@Override
	public String toString() {
		return word + (subMatch == null ? "" : " " + subMatch);
	}
}