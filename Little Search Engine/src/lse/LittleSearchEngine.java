package lse;

import java.io.*;
import java.util.*;

/**
 * This class builds an index of keywords. Each keyword maps to a set of pages in
 * which it occurs, with frequency of occurrence in each page.
 *
 */
public class LittleSearchEngine {
    
    /**
     * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
     * an array list of all occurrences of the keyword in documents. The array list is maintained in 
     * DESCENDING order of frequencies.
     */
    HashMap<String,ArrayList<Occurrence>> keywordsIndex;
    
    /**
     * The hash set of all noise words.
     */
    HashSet<String> noiseWords;
    
    /**
     * Creates the keyWordsIndex and noiseWords hash tables.
     */
    public LittleSearchEngine() {
        keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
        noiseWords = new HashSet<String>(100,2.0f);
    }
    
    /**
     * Scans a document, and loads all keywords found into a hash table of keyword occurrences
     * in the document. Uses the getKeyWord method to separate keywords from other words.
     * 
     * @param docFile Name of the document file to be scanned and loaded
     * @return Hash table of keywords in the given document, each associated with an Occurrence object
     * @throws FileNotFoundException If the document file is not found on disk
     */
    public HashMap<String,Occurrence> loadKeywordsFromDocument(String docFile) 
    throws FileNotFoundException {
        Scanner fileScanner = new Scanner(new File(docFile));
        HashMap<String, Occurrence> map = new HashMap<String,Occurrence>(500, 2.0f);
        while(fileScanner.hasNext()) {
            String candidate = fileScanner.next();
            String key = getKeyword(candidate);
            if(key != null) {
                if(!map.containsKey(key)) {
                    Occurrence value = new Occurrence(docFile, 1);
                    map.put(key, value);
                } else {
                    int frequency = map.get(key).frequency + 1;
                    Occurrence update = new Occurrence(docFile, frequency);
                    map.replace(key, update);
                }
            }
        }
        fileScanner.close();
        return map;
    }
    
    /**
     * Merges the keywords for a single document into the master keywordsIndex
     * hash table. For each keyword, its Occurrence in the current document
     * must be inserted in the correct place (according to descending order of
     * frequency) in the same keyword's Occurrence list in the master hash table. 
     * This is done by calling the insertLastOccurrence method.
     * 
     * @param kws Keywords hash table for a document
     */
    public void mergeKeywords(HashMap<String,Occurrence> kws) {
        for(Map.Entry mapElement : kws.entrySet()) {
            String key = (String) mapElement.getKey();
            Occurrence value = (Occurrence) mapElement.getValue();
            if(keywordsIndex.containsKey(key)) {
                ArrayList<Occurrence> list = keywordsIndex.get(key);
                list.add(value);
                keywordsIndex.replace(key, list);
            } else {
                ArrayList<Occurrence> list = new ArrayList<Occurrence>();
                list.add(value);
                keywordsIndex.put(key, list);
            }
        }
    }
    
    /**
     * Given a word, returns it as a keyword if it passes the keyword test,
     * otherwise returns null. A keyword is any word that, after being stripped of any
     * trailing punctuation(s), consists only of alphabetic letters, and is not
     * a noise word. All words are treated in a case-INsensitive manner.
     * 
     * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
     * NO OTHER CHARACTER SHOULD COUNT AS PUNCTUATION
     * 
     * If a word has multiple trailing punctuation characters, they must all be stripped
     * So "word!!" will become "word", and "word?!?!" will also become "word"
     * 
     * See assignment description for examples
     * 
     * @param word Candidate word
     * @return Keyword (word without trailing punctuation, LOWER CASE)
     */
    public String getKeyword(String word) {
        char[] arr = word.toCharArray();
        if(arr.length == 0) {return null;}
        String key;
        int i = arr.length - 1;
        char nextChar = arr[i];
        while(i > 0 && (nextChar == ',' || nextChar == '.' || nextChar == '?' || nextChar == ':' || nextChar == ';' || nextChar == '!')) {
                i--;
                nextChar = arr[i];
        }
        if(i == 0 && (nextChar == ',' || nextChar == '.' || nextChar == '?' || nextChar == ':' || nextChar == ';' || nextChar == '!')) {
            return null;
        }
        for(int j = 0; j <= i; j++) {
            if(!Character.isLetter(arr[j])) {
                return null;
            }
        }
        key = word.substring(0,(i+1)).toLowerCase();
        if(!noiseWords.contains(key)) {
            return key;
        } else {
            return null;
        }
    }
    
    /**
     * Inserts the last occurrence in the parameter list in the correct position in the
     * list, based on ordering occurrences on descending frequencies. The elements
     * 0..n-2 in the list are already in the correct order. Insertion is done by
     * first finding the correct spot using binary search, then inserting at that spot.
     * 
     * @param occs List of Occurrences
     * @return Sequence of mid point indexes in the input list checked by the binary search process,
     *         null if the size of the input list is 1. This returned array list is only used to test
     *         your code - it is not used elsewhere in the program.
     */
    public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
        if(occs.size() == 1) {return null;}
        ArrayList<Integer> midpoints = new ArrayList<Integer>();
        Occurrence lastOcc = occs.get(occs.size()-1);
        int lastFreq = lastOcc.frequency;
        int l = 0;
        int r = occs.size()-2;
        int m = -1;
        int mFreq = -1;
        while(l <= r) {
            m = (r+l)/2;
            midpoints.add(m);
            mFreq = occs.get(m).frequency; 
            if(mFreq == lastFreq) {
                break;
            } else if(mFreq < lastFreq) {
                r = m-1;
            } else {
                l = m+1;
            }
        }
        if(mFreq <= lastFreq) {
            occs.add(m, lastOcc);
        } else {
            occs.add(m+1, lastOcc);
        }
        occs.remove(occs.size()-1);
        return midpoints;
    }
    
    /**
     * This method indexes all keywords found in all the input documents. When this
     * method is done, the keywordsIndex hash table will be filled with all keywords,
     * each of which is associated with an array list of Occurrence objects, arranged
     * in decreasing frequencies of occurrence.
     * 
     * @param docsFile Name of file that has a list of all the document file names, one name per line
     * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
     * @throws FileNotFoundException If there is a problem locating any of the input files on disk
     */
    public void makeIndex(String docsFile, String noiseWordsFile) 
    throws FileNotFoundException {
        Scanner sc = new Scanner(new File(noiseWordsFile));
        while (sc.hasNext()) {
            String word = sc.next();
            noiseWords.add(word);
        }
        
        sc = new Scanner(new File(docsFile));
        while (sc.hasNext()) {
            String docFile = sc.next();
            HashMap<String,Occurrence> kws = loadKeywordsFromDocument(docFile);
            mergeKeywords(kws);
        }
        sc.close();
    }
    
    /**
     * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
     * document. Result set is arranged in descending order of document frequencies. 
     * 
     * Note that a matching document will only appear once in the result. 
     * 
     * Ties in frequency values are broken in favor of the first keyword. 
     * That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2 also with the same 
     * frequency f1, then doc1 will take precedence over doc2 in the result. 
     * 
     * The result set is limited to 5 entries. If there are no matches at all, result is null.
     * 
     * See assignment description for examples
     * 
     * @param kw1 First keyword
     * @param kw1 Second keyword
     * @return List of documents in which either kw1 or kw2 occurs, arranged in descending order of
     *         frequencies. The result size is limited to 5 documents. If there are no matches, 
     *         returns null or empty array list.
     */
    public ArrayList<String> top5search(String kw1, String kw2) {
        ArrayList<String> top5 = new ArrayList<String>();
        ArrayList<Occurrence> l1 = keywordsIndex.get(kw1);
        ArrayList<Occurrence> l2 = keywordsIndex.get(kw2);
        if(l1 == null && l2 == null) {
            return top5;
        } else {
            int i = 0;
            int j = 0;
            while(top5.size() < 5) {
            	if(l1 != null && l2 != null) {
	                if(i < l1.size() && j < l2.size()) {
	                    if(top5.contains(l1.get(i).document)) {
	                        i++;
	                        continue;
	                    }
	                    if(top5.contains(l2.get(j).document)) {
	                        j++;
	                        continue;
	                    }
	                    int freq1 = l1.get(i).frequency;
	                    int freq2 = l2.get(j).frequency;
	                    if(freq1 >= freq2) {
	                        top5.add(l1.get(i).document);
	                        i++;
	                    } else {
	                        top5.add(l2.get(j).document);
	                        j++;
	                    }
	                } else if(i >= l1.size() && j >= l2.size()){
	                    break;
	                } else if(i < l1.size()) {
	                    if(!top5.contains(l1.get(i).document)) {
	                        top5.add(l1.get(i).document);
	                    }
	                    i++;
	                } else {
	                    if(!top5.contains(l2.get(j).document)) {
	                        top5.add(l2.get(j).document);
	                    }
	                    j++;
	                }
            	} else {
            		if(l1 != null) {
            			if	(i < l1.size()) {
	            			 if(!top5.contains(l1.get(i).document)) {
	 	                        top5.add(l1.get(i).document);
	 	                    }
	 	                    i++;
            			} else {
            				break;
            			}
            		} else if(l2 != null) {
            			if (j < l2.size()) {
            				if(!top5.contains(l2.get(j).document)) {
            					top5.add(l2.get(j).document);
            				}
            				j++;
            			} else {
            				break;
            			}
            		} else {
            			break;
            		}
            	}
            }
        }
        return top5;
    
    }
}