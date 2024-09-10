package text

import java.util.logging.Logger
import java.util.regex.Matcher
import java.util.regex.Pattern;

/**
 *
 * This class is used to extract substring from a string using search phrase and proximity based search.
 *
 * Features:
 * a. It utilizes a basic stopword list.
 * b. It uses regular expression to find a good match first and if a match is not found then it tokenizes the search string
 * and use proximity based search with the help of regular expressions to get best possible match.
 * c. It ignores certain words while looking for search text and also ignores the sequence of the search words.

 * <pre> If the string to search into looks like this:
 * This is some text that needs to be searched for this word or that word. Find the real word now.
 * <b>
 * and the the search string looks like this: 
 * "this word"
 * <b>
 * There are going  be several maches for single words like 'this' and 'word' but only best match will be picked.
 * The best match in this case is full phrase 'this word'. If full phrase is not matched then singe words
 * are used in finding a match.
 *
 * And if the match percentage is greater then 'acceptablePrecisionPercent' property, the match is considered a success.
 * 'AcceptablePrecisionPercent' can be set externally.
 * Match Precision is calculated using formula:
 * Number of matching words found in close proximity / Number of words in search * 100. 
 * Also in case of search for string "word this", same results will hold true as the order of words in the phrase is
 * not important.
 * </pre>
 *
 *  @author Shailender Chohan
 *
 */
public class TextExtractor {
	private static Logger log = Logger.getLogger("TextExtractor");

    private static final String REGEX_BEGIN = "\\b(\\s)?\\(";
	
	/* Ignore plural suffix like s and es at the end of words */
	private static final String REGEX_END = "\\)(es)?(s)?(ing)?(\\s)?\\b";
	
	/* Regular Expressions */
	private static final String  REGEX_TOKENIZE_SEARCH_TEXT = "[\\s,/\\-()]"; 
	private static final String REGEX_FIRST_WORD_IN_BOOKMARK = "[\\w](\\s)?(\\))?(\\])?(\\.)?";
	private static final String REGEX_PICK_NEIGHBORS = "\\b(\\s)?([[a-zA-Z0-9']&&[^\\s]])+(\\s)?\\b";
	private static final String REGEX_STRIP_WHITE_SPACES = "([\t ])+";
	private static final String REGEX_STRIP_RETURN_NEWLINES = "([\r\n]){2,}+";
	private String[] extractedText;
	
	/* The string to be searched to get extraction index */
	private String searchString; 
	
	/* The text from which substring is to be extracted. Used for matching tokens after changing it to UpperCase */
	private String textToExtractFrom;
	
	/* Copy of initial text without change in the case to be used when returning extracted text */
	private String textToExtractFromClone;
	
	/* Set of tokens that are result from breaking search string into tokens */
	private Set searchTextTokens; 
	
	/* List of words that are to be ignored while finding tokens for comparison.*/
	private List ignoreWords;
	
	/* Level of precision that is acceptable. Only matches with values higher then this 
	 * value will be considered a match. */
	private float acceptablePrecisionPercent;
	
	/* Precision percentage achived on match */
	private float achivedPrecisionPercent;
	
	/* If a match is found during search that is greater then acceptablePrecisionPercent 
	 * and less then or equal to achivedPrecisionPercent, keep the first index value  as marker */
	//private boolean exitAtFirstMatch;
	
	/* List of tokens for which match was found */
	ArrayList matchedTokens = null;
	
	/* Proximity preference is used while lookup of 'searchString' in the 'textToExtractFrom' in cases where there are more then
	 * one match for single word. It's value can be between 0 and 100 where 0 indicates beginning of text 'textToExtractFrom' and 
	 * 100 is the end. The actual index value depends upon the size of the string but a value of 50 will always indicate the middle of the text. 
	 * If more then one match is found while searching, this indicator helps in selection of the 
	 * preferred index. Index value nearest to this percentage of this value will be used to return extracted text. 
	 * By default the value is */
	//private int proximityPreference = 0;
	
	/* Determines how many previous words as well as next workds are to be read */
	private int wordReadCount = 0;
	
	/* Assumed average size of each word in tokenized string */
	private final int DEFAULT_WORD_SIZE = 20;
	
	/* List of regular expressions that is to be used for lookup before using any other patterns */
    private ArrayList firstLevelPatterns = null;
    
    // Can be upto 2 only
    private int firstLevelPatternsCount = 2;  
    
    /* Use this regular Expression if it is not null. Otherwise, default regular expression will be used.*/
    //private String regExToUseInSearch;

	/**
	 * Default Constructor for use by containers etc.
	 * Caller must call init() after setting up textToExtract and searchString properties
	 */
	public TextExtractor() {
	} 
	
	/**
	 * Create TextExtractor object
	 * 
	 * @param content Text to extract from
	 * @param searchString  Text to search for to get extraction points
	 */
	public TextExtractor(String content, String searchString) {
		this.setTextToExtractFrom(content);
		this.setSearchString(searchString);
		init();
	}
	
	/**
	 * Initialize member variables. It must be called explicitly if using default Constructor
	 * @return none 
	 */
	public void init() {
		matchedTokens = new ArrayList();
		acceptablePrecisionPercent = 20.0f;
		this.addDefaultIgnoreWords();
		this.searchTextTokens = tokenize(this.searchString, REGEX_TOKENIZE_SEARCH_TEXT);
		wordReadCount = getPreNextWordReadSize(searchTextTokens);
		//exitAtFirstMatch = true;
		firstLevelPatterns = new ArrayList();
	}
	
	/**
	 * Extract text above/before the search string
	 * 
	 * @return Text found above/before the search string is found in the search text.
	 */
	public String extractAbove() {
		if (extractedText == null) {
			extractText();
		}
		if (extractedText != null && extractedText.length > 0 && extractedText[0] != null) {
			return extractedText[0];
		} else {
			return null;
		}
	}
	
	/**
	 * Extract text below/after the search string
	 * @return String Text below/after the match location
	 */
	public String extractBelow() {
		if (extractedText == null) {
			extractText();
		}
		if (extractedText != null && extractedText.length > 1 && extractedText[1] != null) {
			return extractedText[1];
		} else {
			return null;
		}
	}
	/**
	 * Extract text between two strings. Returns null if no match is found for both startText and endText. 
	 * @param textToExtractFrom
	 * @param startText
	 * @param endText
	 * @return String
	 */
	public String extractBetween(String textToExtractFrom, String startText, String endText) {
		boolean foundFirst = true;
		extractedText = null;
		this.setTextToExtractFrom(textToExtractFrom);
		this.setSearchString(startText);
		init();
		StringBuffer sb = new StringBuffer();
		
		if (extractBelow() != null) {
			this.setTextToExtractFrom(extractBelow());
		} else {
			foundFirst = false;
		} 
		this.setSearchString(endText);
		extractedText = null;
		init();
		String extractedAbove = (extractAbove() != null)?extractAbove():(foundFirst)?this.getTextToExtractFrom():null;
		if (extractedAbove == null) {
			return null;
		} else {
			return sb.append(extractedAbove).toString();
		}	
	}
	
	/**
	 * Extract Text and store the results in extractedText array. It can be 
	 * retrived by calling extractAbove() or extractBelow() methods.
	 *
	 */
	public void extractText() {
		log.info("extractText(): invoked");
		buildFirstLevelPatterns();
		if (searchTextTokens == null) {
			throw new IllegalStateException("Please make sure that method tokenize() has been invoked.");
		}
		achivedPrecisionPercent = 0.0f;
		extractedText = getBlocks(); 
	}
	
	/**
	 * Get String blocks. First block represents text above. Second block represents text below. 
	 * In case there was no match, only one block of text is returned.
	 * @return String[] Min size of array is 1 and maximum size is 2.
	 */
	private String[] getBlocks() {
		String[] textBlocks = null;
		String token = null;
		/* Array containing indices where match was found */
		Object[] matchedIndex = null;
		String firstToken = null; // Keep first token value for last comparison if first token is less then 2 characters in size. 
		Object[] allTokens = searchTextTokens.toArray();
		int finalMatchIndex = -1;
		log.info("getBlocks(): Search Token size:" + allTokens.length);
		log.info("getBlocks(): Text to search:" + this.getSearchString());
		
		int i=0; 
		if (allTokens.length < 1) {
			return null;
		}
		// Try to get a match using first level patterns that try to find a match for whole search string
		finalMatchIndex = checkFirstLevelPatternMatch(); 
		log.info("Index using first level patterns: " + finalMatchIndex);
		if (finalMatchIndex >= 0) {
			this.achivedPrecisionPercent = 100.0f;
		} 
		if (finalMatchIndex < 0) {
			/* Take out first token's value as it might be A , b ., i). or something like it. Do it only if it's upto 3 chars.
			 * This token cannot be used for matching all by itself because of it's generic nature */
			if (allTokens[0] != null && allTokens[0].toString().length() < 4 && allTokens[0].toString().matches(REGEX_FIRST_WORD_IN_BOOKMARK) ) {
				log.info("getBlocks(): Ignoring token '" + allTokens[0] + "'. It will not be used for matching unless any of the other tokens match.");
				firstToken = allTokens[0].toString();
				i = 1;
			}
			// Iterate through all the values in the Set and try to get the match in the text
			for (; i < allTokens.length; i++) {
				if (allTokens[i] == null) {
					continue;
				}
				token = allTokens[i].toString();
				matchedIndex = getIndex(textToExtractFrom, token).toArray();
				// Try getting match after collapsing words
				if (matchedIndex.length == 0) {
					String testString = this.getSearchString().replaceAll("\\s*","");
					matchedIndex = getIndex(textToExtractFrom, this.getSearchString().replaceAll("\\s*","")).toArray();
				}
				log.info("getBlocks(): Token[" + i + "]:" + token + "; Mathed " + matchedIndex.length + " times ");
				
				/* Compare words at each index value until we get our complete match */
				for (int j=0; j < matchedIndex.length; j++) {
					log.info("getBlocks(): Compare token:" + token +  " at index:" + matchedIndex[j] + " with its neighbors" );
					if (allTokens.length > 1) {
						/* Read previous few and next words along with the current token word. 
						 * If a complete match is found based on set percentage (@see TextExtractor#acceptablePrecisionPercent) then exit this and outer loops. Otherwise, 
						 * continue the process  for all tokens. 
						 * */
						if (compareNeighborsWithSet(((Integer)matchedIndex[j]).intValue(), token)) {
							log.info("getBlocks(): achivedPrecisionPercent: " + achivedPrecisionPercent + "; acceptablePrecisionPercent:" + acceptablePrecisionPercent);
							log.info("getBlocks(): Match is found at index:" + matchedIndex[j]);
							//if (achivedPrecisionPercent > acceptablePrecisionPercent) {
								finalMatchIndex = ((Integer) matchedIndex[j]).intValue();
							//}
 						}
					 } else {
						/* Since there is one token to match, we'll exit at the first match.*/ 
						finalMatchIndex = ((Integer) matchedIndex[j]).intValue();
							
						break;
					 }	
				}
			}
		} 	
		/* Note: This code does not give precise extraction up to exact first and last words. It includes the matched words for easy identification.
		 * It can be made more precise by uncommenting getSearchString().length in the code below and further processing the output, if needed.*/
		if (finalMatchIndex != -1) {
			textBlocks = new String[2];
			textBlocks[0] =  textToExtractFromClone.substring(0, (finalMatchIndex + getSearchString().length() > textToExtractFromClone.length())?textToExtractFromClone.length(): finalMatchIndex /*+ getSearchString().length()*/);
			textBlocks[1] =  textToExtractFromClone.substring(finalMatchIndex /*+ getSearchString().length()*/, textToExtractFromClone.length());
			log.info("getBlocks(): finalMatchIndex: " + finalMatchIndex);
			log.info("\r\n getBlocks(): Text Above: " + textBlocks[0]);
			log.info("\r\n getBlocks(): Text Below: " + textBlocks[1]);
		}
		return textBlocks;
	}
	
	
	/**
	 * Get array of index values where a match was found. 
	 * Acceptable percentage value is not used by this method.
	 * 
	 * @param toLookInto Text to look into for a match
	 * @param toLookFor  Text to match
	 * @return List of Integer objects
	 */
	public List getIndex(String toLookInto, String toLookFor) {
		ArrayList indexList = new ArrayList();
		int currIndex = 0;
		int index = 0;
		String tempText = null; 
		log.info("getIndex(): To look For: " + toLookFor);
		Pattern p = Pattern.compile(REGEX_BEGIN + toLookFor + REGEX_END);
		Matcher m = p.matcher(toLookInto);

		while (m.find()) {
			index = m.start();
			log.info("getIndex():" + toLookFor + " found at index: " +  index);
			indexList.add(new Integer(index));
			currIndex = index + toLookFor.length();
		}
		return indexList;
	} 
	
	/**
	 * Compare neighboring words of the token parameter with the member tokenized Set. 
	 * @param currIndex
	 * @param token
	 * @return True if the value of achivedPrecision was updated in getStats().
	 */
	private boolean compareNeighborsWithSet(int currIndex, String token) {
		log.info("compareNeighborsWithSet(): CurrIndex: " + currIndex + " ; Token: " + token);
		List neighbors = getNeighbors(this.textToExtractFrom, token.trim(), currIndex, wordReadCount, true);
		/* There was no match. Return false. */
		if (neighbors == null) {
			return false;
		}
		List backNeighbors = getNeighbors(this.textToExtractFrom, token.trim(), currIndex, wordReadCount, false);
		if (backNeighbors != null) {
			neighbors.addAll(backNeighbors);
		}
		log.info("compareNeighborsWithSet(): Neighbors: " + neighbors + " for token: " + token);
		return getMatchStats(new HashSet(neighbors));
	}
	
	/**
	 * 	
	 * Returns neighboring words of 'toLookFor' from the current index position. It does not return
	 * the 'toLookFor' in the List if 'lookAhead' is false. Otherwise, it returns 'toLookFor'.  
	 *   
	 * @param textToLook Text to search in
	 * @param toLookFor Text to look for
	 * @param currIndex Index from where to begin the search
	 * @param numberOfWords Number of words to be returned.
	 * @param lookAhead True if lookahead, false if look behind 
	 * @return List of 'toLookFor' neighbors. Null if 'toLookFor' is not found in 'textToLook'.
	 */
	public List getNeighbors(String textToLook, String toLookFor, int currIndex, int numberOfWords, boolean lookAhead) {
		if (numberOfWords < 1) { 
			return null;
		}
		/* Using DEFAULT_WORD_SIZE characters as size for an average word. */ 
		int charToRead = DEFAULT_WORD_SIZE * numberOfWords + toLookFor.length();
		ArrayList alWords = new ArrayList();
		int nextIndex = currIndex;
		int upperLimit = (charToRead + currIndex) < textToLook.length()? (charToRead + currIndex):textToLook.length();
		int lowerLimit = (currIndex - charToRead) > 0?(currIndex - charToRead):0;
		int indexOftoLookForInList = -1;
		String addWord = "";
		log.info("Finding neighbors from index:" + lowerLimit + " to index:" + upperLimit);

		/* Get subset of text to break into words so that words can be compared against other words in the main token set */
		textToLook = (lookAhead)?textToLook.substring(currIndex, upperLimit):textToLook.substring(lowerLimit, currIndex);
		Pattern p = Pattern.compile(REGEX_PICK_NEIGHBORS);
		Matcher m = p.matcher(textToLook);
		List wordList = new ArrayList();
		log.info("Finding neighbors in:" + textToLook.replaceAll("[(\\s+)\r\n]"," "));
		
		/* Add newly tokenized words to a list */ 
		while(m.find()) {
			addWord = textToLook.substring(m.start(), m.end()).trim();
			if (ignoreWords.contains(addWord)) {
				continue;
			}
			if (toLookFor.equalsIgnoreCase(addWord)) {
				indexOftoLookForInList = alWords.size(); 
			}
			alWords.add(addWord.toUpperCase()); 
		}
		
		/* indexOftoLookForInList can be -1 only if lookAhead is false.*/
		if (indexOftoLookForInList == -1 && lookAhead) {
			return null; 
		}
		if (lookAhead) {
			if (indexOftoLookForInList+numberOfWords+1 >= alWords.size()) {
				return alWords.subList(indexOftoLookForInList, alWords.size());
			} else {
				return alWords.subList(indexOftoLookForInList, indexOftoLookForInList+numberOfWords+1);
			}	
		} else {
			if (alWords.size() >= numberOfWords) {
				return alWords.subList(alWords.size()-numberOfWords, alWords.size());
			} else {
				return alWords.subList(0, alWords.size());
			}	
		}
	} 
	
	/**
	 * Match the elements in passed set against the searchTextTokens Set and store 
	 * percentage of number of elements that matched in achivedPrecisionPercent.
	 * 
	 * @param subStringTokens Set of string tokens to match against this object's string tokens
	 * @return boolean True if achivedPrecisionPercent value was updated i.e. if the current match was 
	 * of higher percent value than any of previous matches.
	 */
	private boolean getMatchStats(Set subStringTokens) {
		Object[] searchTokensArray = searchTextTokens.toArray(); 
		int matchCount = 0;
		boolean valueUpdated = false;
		for (int i=0; i < searchTokensArray.length; i++) {
			//TODO Fixit at the time it is tokenized. We should not get any . after a string. 
			if (searchTokensArray[i] != null && searchTokensArray[i].toString().length() > 1 && searchTokensArray[i].toString().charAt(searchTokensArray[i].toString().length()-1) == '.') {
				searchTokensArray[i] = searchTokensArray[i].toString().substring(0, searchTokensArray[i].toString().length()-1);
			}
			if (subStringTokens.contains(searchTokensArray[i])) {
				matchCount++;
			}
		} 
		float tempPercent = (float) matchCount / searchTokensArray.length * 100.0f;
		valueUpdated = (tempPercent > achivedPrecisionPercent) && (tempPercent > acceptablePrecisionPercent);
		achivedPrecisionPercent = (valueUpdated)? tempPercent:achivedPrecisionPercent;
		log.info("getMatchStats(): Matched: " + matchCount + " of " +  searchTokensArray.length + " picked: " + valueUpdated);
		return valueUpdated; 
	}   
	
	/**
	 * Tokenize the input string and put tokens in a Set
	 * @param stringToTokenize
	 * @param regEx Regular Expression to use to tokenize the string.
	 * @return Set of string tokens
	 */
	public Set tokenize(String stringToTokenize, String regEx) {
		LinkedHashSet s = new LinkedHashSet();
		String[] tokens = stringToTokenize.split(regEx);
		
		for (int i=0; i<tokens.length; i++) {
			if (!ignoreWords.contains(tokens[i]) && !"".equals(tokens[i].trim())) {
				s.add(tokens[i].toUpperCase());
			}
		}
		log.info("Tokens to search for:" + s);
		return s;
	}
	
	/**
	 * First level of search is executed using a simple pattern that consist of all
	 * the bookmark text ignoring white space between the text.
	 * If we do not find a match using this pattern, then we break the search
	 * phrase to multiple tokens and try to find a match for as many tokens as possible
	 * if they are in close proximity.
	 */
	public void buildFirstLevelPatterns() {
	       Pattern pattern = Pattern.compile("\\s+");
           String input2 = searchString.replaceAll("\\(|\\)|,|/|-", " ");   
           int PATCOUNT = this.getFirstLevelPatternsCount();
           
           for(int p=0; p < PATCOUNT; p++) {
               int index = (p==1) ? 1 : 0;
               String[] tokens = (p==0) ? (pattern.split(searchString.trim())) : (pattern.split(input2.trim()));               
               StringBuffer regex = new StringBuffer(1024);
               int len = tokens.length;
               log.info("buildFirstLevelPatterns():tokens:  " + tokens.length + " Pattern Level Count: " + PATCOUNT);
               
               if (len < 2) {
               	 return;
               }
               switch (p) {
                    case 0: 
                         for (int i= index; i < len; i++) {
                              if (i != len-1) {
                                   regex.append("\\b" + tokens[i] + "\\s+");
                              } else {
                                   regex.append("\\b" + tokens[i]);
                              }     
                         }
                         break;     
                    case 1:
                    	regex.append(tokens[0]);
                    	for (int i= index; i < len; i++) {
                                regex.append("\\s*" + tokens[i] + "\\s*");
                         } 
                         break;
                    default: break;
               }    //end switch
               log.info("Added First Level Pattern:  " + regex.toString());	
               firstLevelPatterns.add(regex.toString());            
           }
	}
	
	public void addToFirstLevelPatterns(String newPattern) {
		firstLevelPatterns.add(newPattern);
	}

	/**
	 * List of default ignore words. List can be set externally by caller of this object.
	 * These words, if present in the text to look for, are not tokenized and hence ignored 
	 * while looking for a match. 
	 *
	 */
	private void addDefaultIgnoreWords() {
		ignoreWords = new ArrayList();
		ignoreWords.add("and");
		ignoreWords.add("or");
		ignoreWords.add("the");
		ignoreWords.add("\\");
		ignoreWords.add("//");
		ignoreWords.add("-");
		ignoreWords.add("at");
		ignoreWords.add("on");
		ignoreWords.add("in");
		ignoreWords.add("to");
		ignoreWords.add("do");
		ignoreWords.add("not");
		ignoreWords.add("as");
		ignoreWords.add("by");
		ignoreWords.add("for");
		ignoreWords.add("of");
		ignoreWords.add("it");
		ignoreWords.add("any");
		ignoreWords.add(",");
		ignoreWords.add(".");
		ignoreWords.add("other");
		// TODO add more to it if needed and move it to properties file
	}

	public List getIgnoreWords() {
		return ignoreWords;
	}
	
	public void setIgnoreWords(List ignoreWords) {
		this.ignoreWords = ignoreWords;
	}
	
	public float getAcceptablePrecisionPercent() {
		return acceptablePrecisionPercent;
	}
	
	public void setAcceptablePrecisionPercent(float acceptablePrecisionPercent) {
		acceptablePrecisionPercent = (acceptablePrecisionPercent > 100)?100.0f:(acceptablePrecisionPercent < 0)? 1.0f:acceptablePrecisionPercent;   
		this.acceptablePrecisionPercent = acceptablePrecisionPercent;
	}
	
	public String getSearchString() {
		return searchString;
	}
	
	/**
	 * Removes ()[] characters from the string before using it to search.
	 * @param searchString
	 */
	private void setSearchString(String searchString) {
		//searchString = searchString.replaceAll("[()\\]\\[]","");
		if (searchString != null) {
			this.searchString = searchString;//searchString.toLowerCase();
		} else {
			this.searchString = "";
		}	
	}
	
	public String getTextToExtractFrom() {
		return textToExtractFromClone;
	}
	
	private void setTextToExtractFrom(String textToExtractFrom) {
		/* Make a copy of it when is set for the first time*/
		textToExtractFromClone = formatString(textToExtractFrom);
		if (textToExtractFrom != null) {
			this.textToExtractFrom = formatString(textToExtractFrom.toUpperCase());
			
		} else {
			this.textToExtractFrom = "";
		}	
	}
	
	/*public boolean getExitAtFirstMatch() {
		return exitAtFirstMatch;
	}
	
	public void setExitAtFirstMatch(boolean exitAtFirstMatch) {
		this.exitAtFirstMatch = exitAtFirstMatch;
	}*/
	
	private int getPreNextWordReadSize(Set tokens) {
	    return (tokens.size() > 1)?tokens.size()-1:tokens.size();
	}
	
	public int getFirstLevelPatternsCount() {
		return this.firstLevelPatternsCount;
	}
	public void setFirstLevelPatternsCount(int firstLevelPatternsCount) {
		if (firstLevelPatternsCount < 1) {
			this.firstLevelPatternsCount =  1;
		} else if (firstLevelPatternsCount > 2) {
			this.firstLevelPatternsCount =  2;
		} 
		this.firstLevelPatternsCount = firstLevelPatternsCount;
	}

	
	
	/**
	 * Return index of location where match was found.
	 * 
	 * @return index
	 */
	private int checkFirstLevelPatternMatch() {
        Pattern pat = null;
        String reg = null, result = "";
        int index = -1;
        Iterator it = firstLevelPatterns.iterator();
        log.info("checkFirstLevelPatternMatch(): firstLevelPatterns.size(): " + firstLevelPatterns.size()); 
        
        while (it.hasNext()) {
             reg = (String) it.next();
             pat = Pattern.compile(reg, Pattern.CASE_INSENSITIVE|Pattern.MULTILINE|Pattern.DOTALL);
             Matcher matcher = pat.matcher(this.getTextToExtractFrom());
             log.info("checkFirstLevelPatternMatch(): Matching pattern: " + reg);
             if (matcher.find()) {
                  index = matcher.start();
                  achivedPrecisionPercent = 100.0f;
                  log.info("checkFirstLevelPatternMatch(): Match found using first level patterns; Index:" + index + "; Regular Expression Used:" + reg);
                  break;
             }
        }    
        return index;
	}

	private String formatString(String input) {
		if (input == null || input == "") {
			return input;
		}
		// Replace more then one newline and return characters with single character
		String tempString = input.replaceAll(REGEX_STRIP_RETURN_NEWLINES, "\n");
		// Replace multiple white spaces with one whitespace
		return tempString.replaceAll(REGEX_STRIP_WHITE_SPACES, " ");
	}


    public static void main(String[] args) {
        TextExtractor textExtractor = new TextExtractor()
        textExtractor.setTextToExtractFrom("Creative Services Specialist\n" +
                "Creative Services\n" +
                "\n" +
                "Beverly Hills, CA\n" +
                "\n" +
                "As part of the Digital Supply Chain, the Creative Services team is responsible for communicating with studio partners to acquire, approve, design, and deploy all artwork necessary for the Netflix service.\n" +
                "Creative Services Specialists are responsible for owning and improving several studio or content provider relationships to facilitate the end-to-end artwork acquisition workflow. This workflow is evolving in exciting new ways and becoming increasingly complex and interesting as Netflix pioneers new ways to merchandise content to subscribers in a digital medium.\n" +
                "A Specialist works with guidance from the Creative Services Manager to educate studios or content providers on our Netflix Style Guide which details all assets required by the Netflix service. The Specialist is responsible for ensuring the quality of the received assets, facilitating design work for images as necessary, seeking any necessary approval from partners, and delivering all assets to the Netflix service. Specialists should constantly strive to improve the delivery workflow and artwork quality by regularly communicating with and supporting our partners.\n" +
                "\n" +
                "The ideal Creative Services Specialist candidate is a self-motivated worker who thrives in an environment that relies on his/her self-discipline and ability to set and meet simultaneous daily and long-term deadlines. S/he is extremely detail-oriented and a strong communicator who responds well in a fast-paced environment. With shifting priorities and multiple deadlines, this position requires a high level of multi-tasking and follow-through to assure that all assets are acquired and approved for Netflix use.\n" +
                "The position is on site in our Beverly Hills offices.\n" +
                "Qualifications:\n" +
                "- Very strong ability to problem solve, multitask, and prioritize in a fast paced, high volume environment\n" +
                "- Minimum 3 years of experience dealing with digital assets and partner relationships preferably in a studio or technology related environment\n" +
                "- Extensive experience utilizing a Digital Asset Management system or similar tool set to propagate and track assets through an operational workflow\n" +
                "- Background with the Adobe Creative Suite (Photoshop, Bridge and In Design) preferred\n" +
                "- Ability to manage multiple time-sensitive tasks simultaneously and independently\n" +
                "- Prior project management, team leadership experience, or agency management\n" +
                "- Excellent verbal and written communication skills\n" +
                "- Advanced proficiency with Microsoft Office Suite\n" +
                "- Strong attention to detail\n" +
                "Job Responsibilities:\n" +
                "- Review, prioritize, and track digital assets from partner submission to the delivery of the content to the customer\n" +
                "- Build and strengthen relationships with studios and content providers\n" +
                "- Effectively communicate and execute across several cross-functional teams\n" +
                "- Independently execute and use discretion to escalate issues in a timely and appropriate manner\n" +
                "- Display courage in making decisions or voicing opinions, even if unpopular\n" +
                "- Understand, advocate, and embody the company�s values and team goals\n" +
                "- Aggregate and review metrics to strengthen and improve yourself and the team\n" +
                "- Consistently strive to review the current operational workflow and identify efficiencies\n" +
                "Education:\n" +
                "- B.A. / B.S. Required")

            textExtractor.setSearchString("Responsibilities Job")
            textExtractor.init()

            textExtractor.extractText()

            println textExtractor.extractedText


    }
}
