package com.invisiblearchitecture.scraper;

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
		private final String destinationInformationType;
		private final String destinationNamespace;
		public ToInformation(InformationFactory iF, String sf, PatternInterface pat, String dns, String dit, String df) {
			super(sf, pat, df);
			destinationNamespace = dns;
			factory = iF;
			destinationInformationType = dit;
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
				Information[] childInformations = new Information[results.length];
				for(int i = 0; i < results.length; i++) {
					childInformations[i] = factory.get(destinationNamespace, destinationInformationType);
					childInformations[i].putField(destinationField, results[i]);
					childInformations[i].interpret(); // We don't recursively collect here -- that's the publisher's job.
				}
				// De-namespace inside the child listing.
				sourceInformation.addChildInformations(destinationInformationType, childInformations);
				return true;
			}
		}
		public String toString() {
			return sourceField + " -> Information " + destinationInformationType + '.' + destinationField;
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
			return "No Pattern";
		} else {
			return pattern.toString();
		}
	}
}
