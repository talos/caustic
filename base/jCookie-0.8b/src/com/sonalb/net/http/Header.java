package com.sonalb.net.http;

import java.util.Vector;
import java.util.Iterator;
import java.util.Collection;
import java.util.AbstractCollection;

/**
 * Represents the Header of an HTTP Message. An HTTP header usually consists of two 
 * major components:
 * <ul>
 * <li>The first line containing HTTP-Command, HTTP-Version etc.</li>
 * <li>A number of name-value pairs</li>
 * </ul>
 * This Header class does not consider the first line as a name-value pair as the HttpURLConnection 
 * class does.<br>
 * NULL name fields in a pair are not permitted.
 * @author Sonal Bansal
 */

public class Header extends AbstractCollection implements java.io.Serializable, java.lang.Cloneable
{
	private String topLine;
	private Vector theHeader;

	/**
	 * Creates an empty Header.
	 */
	public Header()
	{
		topLine = null;
		theHeader = new Vector();
	}

	/**
	 * Creates a Header, and populates it with HeaderEntries from input Collection.
	 * @param c the Collection containing HeaderEntry objects
	 */
	public Header(Collection c)
	{
		topLine = null;
		theHeader = new Vector();
		addAll(c);
	}

	/**
	 * Creates a Header with given top-line, and populates it with HeaderEntries from input Collection.
	 * @param c the Collection containing HeaderEntry objects
	 * @param topLine the top-most line in an HTTP Header
	 */
	public Header(String topLine, Collection c)
	{
		this.topLine = topLine;
		theHeader = new Vector();
		addAll(c);
	}

	/**
	 * Sets the top-line of this Header.
	 * @param topLine the top-most line in an HTTP Header
	 */
	public void setTopLine(String topLine)
	{
		this.topLine = topLine;
	}

	/**
	 * Gets the top-line of this Header.
	 * @return the top-most line in an HTTP Header
	 */
	public String getTopLine()
	{
		return(topLine);
	}

	/**
	 * Adds the specified key-value pair to this Header.
	 */
	public boolean add(String key, String value)
	{
		return(add(new HeaderEntry(key,value)));
	}

	/**
	 * Returns the entry at specified index.
	 */
	public HeaderEntry getEntryAt(int index)
	{
		return((HeaderEntry) theHeader.get(index));
	}

	/**
	 * Returns the header key for entry at specified index.
	 */
	public String getHeaderFieldKey(int i)
	{
		return(getEntryAt(i).getKey());
	}

	/**
	 * Returns the header value for entry at specified index.
	 */
	public String getHeaderField(int i)
	{
		return(getEntryAt(i).getValue());
	}

	/**
	 * Checks whether any header entry exists with given key.
	 */
	public boolean containsKey(String s)
	{
		if(s == null)
		{
			throw new IllegalArgumentException("Key can't be null");
		}

		HeaderEntry he;

		Iterator iter = iterator();

		while(iter.hasNext())
		{
			he = (HeaderEntry) iter.next();

			if(s.equalsIgnoreCase(he.getKey()))
			{
				return(true);
			}
		}

		return(false);
	}

	/**
	 * Checks whether any header entry exists with given value.
	 */
	public boolean containsValue(String s)
	{
		HeaderEntry he;

		Iterator iter = iterator();

		while(iter.hasNext())
		{
			he = (HeaderEntry) iter.next();

			if(s == null)
			{
				if(he.getValue() == null)
				{
					return(true);
				}
			}
			else if(s.equals(he.getValue()))
			{
				return(true);
			}
		}

		return(false);
	}

	/**
	 * Returns the HeaderEntry corresponding to the first occurrence of the given key.
	 */
	public HeaderEntry getFirstEntryForKey(String s)
	{
		return(getEntryForKey(s,-1));
	}

	/**
	 * Returns the HeaderEntry corresponding to the first occurrence of the given value.
	 */
	public HeaderEntry getFirstEntryForValue(String s)
	{
		return(getEntryForValue(s,-1));
	}

	/**
	 * Returns the HeaderEntry corresponding to the first occurrence of the given key, after specified index (non-inclusive).
	 */
	public HeaderEntry getEntryForKey(String s, int j)
	{
		if(s == null)
		{
			throw new IllegalArgumentException("Key can't be null");
		}

		if(j < 0)
		{
			j = 0;
		}
		else if(j > theHeader.size() - 1)
		{
			j = theHeader.size() - 1;
		}

		HeaderEntry he;

		for(int i=j+1; i < theHeader.size(); i++)
		{
			try
			{
				he = getEntryAt(i);
			}
			catch(IndexOutOfBoundsException ioobe)
			{
				break;
			}

			if(s.equalsIgnoreCase(he.getKey()))
			{
				return(he);
			}
		}

		return(null);
	}

	/**
	 * Returns the HeaderEntry corresponding to the first occurrence of the given value, after specified index (non-inclusive).
	 */
	public HeaderEntry getEntryForValue(String s, int j)
	{
		HeaderEntry he;

		if(j < 0)
		{
			j = 0;
		}
		else if(j > theHeader.size() - 1)
		{
			j = theHeader.size() - 1;
		}

		for(int i=j+1; i < theHeader.size(); i++)
		{
			try
			{
				he = getEntryAt(i);
			}
			catch(IndexOutOfBoundsException ioobe)
			{
				break;
			}

			if(s == null)
			{
				if(he.getValue() == null)
				{
					return(he);
				}
			}
			else if(s.equalsIgnoreCase(he.getValue()))
			{
				return(he);
			}
		}

		return(null);
	}

	/**
	 * Returns a Header consisting of all HeaderEntries having given key.
	 */
	public Header getEntriesForKey(String s)
	{
		if(s == null)
		{
			throw new IllegalArgumentException("Key can't be null.");
		}

		HeaderEntry he;
		Header h = new Header();

		Iterator iter = iterator();

		while(iter.hasNext())
		{
			he = (HeaderEntry) iter.next();

			if(s.equalsIgnoreCase(he.getKey()))
			{
				h.add(he);
			}
		}

		return(h);
	}

	/**
	 * Returns a Header consisting of all HeaderEntries having given value.
	 */
	public Header getEntriesForValue(String s)
	{
		HeaderEntry he;
		Header h = new Header();

		Iterator iter = iterator();

		while(iter.hasNext())
		{
			he = (HeaderEntry) iter.next();

			if(s == null)
			{
				if(he.getValue() == null)
				{
					h.add(he);
				}
			}
			else if(s.equals(he.getValue()))
			{
				h.add(he);
			}
		}

		return(h);
	}

	public boolean add(Object entry)
	{
		if(entry == null)
		{
			throw new IllegalArgumentException("Null entry.");
		}
		else if(!(entry instanceof HeaderEntry))
		{
			throw new ClassCastException("Not a HeaderEntry");
		}

		if(contains(entry))
			return(false);

		theHeader.add(entry);

		return(true);
	}

	public Iterator iterator()
	{
		return(theHeader.iterator());
	}

	public int size()
	{
		return(theHeader.size());
	}
					
	public String toString()
	{
		if(isEmpty())
		{
			return("{}");
		}

		StringBuffer sb = new StringBuffer();

		sb.append("{");

		if(topLine != null)
		{
			sb.append(topLine);
			sb.append("\n[");
		}

		for(int i=0; i < theHeader.size(); i++)
		{
			sb.append(theHeader.get(i).toString());
			sb.append(",");
		}

		sb.deleteCharAt(sb.length()-1);
		sb.append("]}");

		return(sb.toString());
	}

	public boolean isEmpty()
	{
		return(theHeader.size() == 0 && topLine == null);
	}

	public Object clone() throws CloneNotSupportedException
	{
		return(super.clone());
	}
}