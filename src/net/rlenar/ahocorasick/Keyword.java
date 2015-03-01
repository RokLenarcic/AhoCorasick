package net.rlenar.ahocorasick;

final class Keyword {
	Keyword alsoContains;
	final String word;

	Keyword(final String word) {
		this.word = word;
	}

	@Override
	public String toString() {
		return word + (alsoContains == null ? "" : " " + alsoContains);
	}
}