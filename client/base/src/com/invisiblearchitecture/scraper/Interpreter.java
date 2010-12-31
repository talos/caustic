package com.invisiblearchitecture.scraper;

import java.io.IOException;

public abstract class Interpreter {
	protected final String sourceField;
	protected final PatternInterface pattern;
	protected final String destinationField;
	
	public Interpreter(String sf, PatternInterface p, String df) {
		sourceField = sf;
		pattern = p;
		destinationField = df;
	}
	
	public static class ToField extends Interpreter {
		private int matchNumber;
		public ToField(String sf, PatternInterface pat, int mn, String df) {
			super(sf, pat, df);
			matchNumber = mn;
		}
		public Boolean read(Information sourceInformation) {
			String result;
			String input = getInput(sourceInformation);
			if(input == null)
				return null;
			if(pattern != null) {
				result = pattern.match(input, matchNumber);
			} else {
				result = input;
			}
			if(result != null) {
				sourceInformation.putField(destinationField, result);
				return true;
			} else {
				return false;
			}
		}
		public String toString() {
			return sourceField + " -> " + destinationField;
		}
	}
	
	public static class ToInformation extends Interpreter {
		private final InformationFactory factory;
		//private final String destinationInformationType;
		//private final String destinationNamespace;
		private final String[][] targets;
		/* Old style: single target area, single target type. */
		public ToInformation(InformationFactory iF, String sf, PatternInterface pat, String target_area, String target_type, String df) {
			super(sf, pat, df);
			targets = new String[1][2];
			targets[0][0] = target_area;
			targets[0][1] = target_type;
			factory = iF;
		}
		/* New style: array of targets (area then type). */
		public ToInformation(InformationFactory iF, String sf, PatternInterface pat, String[][] t, String df) {
			super(sf, pat, df);
			factory = iF;
			targets = t;
		}
		public Boolean read(Information sourceInformation) {
			String[] results;
			String input = getInput(sourceInformation);
			if(input == null)
				return null;
			if(pattern != null) {
				results = pattern.allMatches(input);
			} else {
				results = new String[1];
				results[0] = input;
			}
			if(results == null) {
				return false;
			} else {
				Information[] childInformations = new Information[results.length * targets.length];
				for(int i = 0; i < results.length; i++) {
					try {
						for(int j = 0; j < targets.length; j++) {
							int childNum = (targets.length * i) + j;
							childInformations[childNum] = factory.get(targets[j][0], targets[j][1]);
							childInformations[childNum].putField(destinationField, results[i]);
							childInformations[childNum].interpret(); // We don't recursively collect here -- that's the publisher's job.	
							// De-type inside the child listing.
							sourceInformation.addChildInformations(targets[j][1], new Information[] {childInformations[childNum]});
						}
					} catch(IOException e) {
						//logger.e("Error creating child information.", e);
						return false;
					}
				}
				return true;
			}
		}
		public String toString() {
			return sourceField + " -> Information " + targets.length + " targets' " + destinationField;
		}
	}
	
	protected String getInput (Information sourceInformation) {
		String input = sourceInformation.getField(sourceField);
		return input;
	}
	/**
	 * Have an interpreter attempt to read Information.
	 * @param sourceInformation
	 * @return True if successful, False if unsuccessful, Null if the information isn't ready yet.
	 */
	public abstract Boolean read(Information sourceInformation);
	
	public String patternString() {
		if(pattern == null) {
			return "[No Pattern]";
		} else {
			return pattern.toString();
		}
	}
}
