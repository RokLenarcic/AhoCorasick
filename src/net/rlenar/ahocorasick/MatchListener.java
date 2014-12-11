package net.rlenar.ahocorasick;

public interface MatchListener {

	// return true to continue matching
	boolean match(final String word, final int endPosition);

}
