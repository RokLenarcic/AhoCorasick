package net.rlenar.ahocorasick;

final class Match {
	Match alsoMatches;
	final String word;

	Match(final String word) {
		this.word = word;
	}

	@Override
	public String toString() {
		return word + (alsoMatches == null ? "" : " " + alsoMatches);
	}
}