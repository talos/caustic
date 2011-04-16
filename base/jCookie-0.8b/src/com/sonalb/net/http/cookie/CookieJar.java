package com.sonalb.net.http.cookie;

import java.util.Vector;
import java.util.Iterator;
import java.util.Collection;
import java.net.URL;
import com.sonalb.Utils;

/*
According to RFC2965, if Set-Cookie and set-cookie2 both describe the same cookie, then sc2 should be used.
	This distinction has not been incorporated.

ADD function to getcookies by version
*/

/**
 * Container for <code>Cookie</code> objects. Each CookieJar is independent of any request. 
 * This means that a single CookieJar can hold all the cookies for a number of requests and servers.
 * @author	Sonal Bansal
 */

public class CookieJar implements java.util.Collection, java.io.Serializable
{
	private Vector theJar;
	private int iNumCookies;
	
	/**
	 * Creates an empty CookieJar.
	 */
	public CookieJar()
	{
		theJar = new Vector();
		iNumCookies = 0;
	}

	/**
	 * Creates a CookieJar, and populates it with Cookies from input Collection. All the objects in 
	 * the input Collection NEED NOT be Cookie objects.
	 * @param c the input Collection
	 */
	public CookieJar(Collection c)
	{
		theJar = new Vector();
		iNumCookies = 0;
		addAll(c);
	}

	protected CookieJar(int initialCapacity, int growthStep)
	{
		theJar = new Vector(initialCapacity,growthStep);
		iNumCookies = 0;
	}

	public boolean add(Object o)
	{
		if(o == null)
		{
			throw new IllegalArgumentException("Null cookie.");
		}
		else if(!(o instanceof Cookie))
		{
			throw new ClassCastException("Not a Cookie.");
		}

		Cookie cookie;

		try
		{
			cookie = (Cookie) ((Cookie) o).clone();
		}
		catch(CloneNotSupportedException cnse)
		{
			throw new IllegalArgumentException("Could not add. Object does not support Cloning.");
		}

		if(!cookie.isValid())
		{
			throw new IllegalArgumentException("Invalid cookie.");
		}

		int ind = getCookieIndex(cookie);

		if(ind == -1)
		{
			theJar.add(cookie);
			iNumCookies++;
		}
		else
		{
			theJar.setElementAt(cookie, ind);
		}

		return(true);
	}

	public boolean addAll(Collection c)
	{
		if(c == null)
		{
			throw new IllegalArgumentException("Null Collection");
		}

		if(!c.isEmpty())
		{
			Iterator iter = c.iterator();

			while(iter.hasNext())
			{
				try
				{
					add(iter.next());
				}
				catch(Exception e)
				{
				}
			}
		}
		else
		{
			return(false);
		}

		return(true);
	}

	public Iterator iterator()
	{
		return(theJar.iterator());
	}

	public boolean contains(Object o)
	{
		if(o == null)
		{
			throw new IllegalArgumentException("Null cookie");
		}
		else if(!(o instanceof Cookie))
		{
			throw new ClassCastException("Not a cookie");
		}

		Cookie c = (Cookie) o;

		if(!c.isValid())
		{
			throw new IllegalArgumentException("Invalid cookie.");
		}

		return(theJar.contains(c));
	}

	public boolean containsAll(Collection c)
	{
		if(c != null)
		{
			Iterator iter = c.iterator();

			while(iter.hasNext())
			{
				if(!contains(iter.next()))
				{
					return(false);
				}
			}
		}
		else
		{
			throw new IllegalArgumentException("Null collection");
		}

		return(true);
	}

	public Object[] toArray()
	{
		return(theJar.toArray());
	}

	public Object[] toArray(Object[] array)
	{
		if(array == null)
		{
			throw new IllegalArgumentException("Null array.");
		}

		Cookie[] cookieArray = new Cookie[array.length];

		try
		{
			for(int i=0; i<array.length; i++)
			{
				cookieArray[i] = (Cookie) array[i];
			}
		}
		catch(ClassCastException cce)
		{
			throw new ArrayStoreException("ClassCastException occurred.");
		}

		return(theJar.toArray(cookieArray));
	}

	public void clear()
	{
		theJar.clear();
		iNumCookies = 0;
	}

	public boolean removeAll(Collection c)
	{
		if(c == null)
		{
			throw new IllegalArgumentException("Null collection");
		}

		if(!c.isEmpty())
		{
			Iterator iter = c.iterator();

			while(iter.hasNext())
			{
				remove(iter.next());
			}
		}
		else
		{
			return(false);
		}

		return(true);
	}

	public boolean retainAll(Collection c)
	{
		if(c == null)
		{
			throw new IllegalArgumentException("Null collection");
		}

		if(!c.isEmpty())
		{
			Iterator iter = c.iterator();
			Object o;

			while(iter.hasNext())
			{
				o = iter.next();
				if(!contains(o))
				{
					remove(o);
				}
			}
		}
		else
		{
			return(false);
		}

		return(true);
	}

	public boolean remove(Object o)
	{
		if(o == null)
		{
			throw new IllegalArgumentException("Null cookie.");
		}
		else if(!(o instanceof Cookie))
		{
			throw new ClassCastException("Not a cookie.");
		}

		Cookie cookie = (Cookie) o;

		if(!cookie.isValid())
		{
			throw new IllegalArgumentException("Invalid cookie.");
		}

		return(theJar.remove(cookie));
	}

	/**
	 * Removes all cookies that match the given CookieMatcher.
	 * @param cm the CookieMatcher
	 */
	public void removeCookies(CookieMatcher cm)
	{
		if(cm == null)
		{
			throw new IllegalArgumentException("Null CookieMatcher");
		}

		Cookie c;

		for(int i=0; i < iNumCookies; i++)
		{
			c = (Cookie) theJar.get(i);
			if(cm.doMatch(c))
			{
				theJar.removeElementAt(i);
				iNumCookies--;
			}
		}
	}

	protected int getCookieIndex(Cookie c)
	{
		int retVal = -1;

		for(int i=0; i < iNumCookies; i++)
		{
			if(c.equals(theJar.get(i)))
			{
				retVal = i;
				break;
			}
		}

		return(retVal);
	}

	public int size()
	{
		if(iNumCookies > Integer.MAX_VALUE)
		{
			return(Integer.MAX_VALUE);
		}

		return(iNumCookies);
	}
					
	public boolean isEmpty()
	{
		return(iNumCookies == 0);
	}

	/**
	 * Gets all Cookies that match the given CookieMatcher.
	 * @param cm the CookieMatcher
	 * @return the CookieJar with matching cookies; always non-null
	 */
	public CookieJar getCookies(CookieMatcher cm)
	{
		if(cm == null)
		{
			throw new IllegalArgumentException("Invalid CookieMatcher");
		}

		CookieJar cj = new CookieJar();
		Cookie c;

		for(int i=0; i < iNumCookies; i++)
		{
			c = (Cookie) theJar.get(i);
			if(cm.doMatch(c))
			{
				cj.add(c);
			}
		}

		return(cj);
	}

	/**
	 * Gets all Cookies with the given name.
	 * @param cookieName the cookie name
	 * @return the CookieJar with matching cookies; always non-null
	 */
	public CookieJar getCookies(String cookieName)
	{
		if(Utils.isNullOrWhiteSpace(cookieName))
		{
			throw new IllegalArgumentException("Name cannot be empty");
		}

		CookieJar cj = new CookieJar();
		Cookie c;

		for(int i=0; i < iNumCookies; i++)
		{
			c = (Cookie) theJar.get(i);
			if(cookieName.equalsIgnoreCase(c.getName()))
			{
				cj.add(c);
			}
		}

		return(cj);
	}

	/**
	 * Gets all Cookies having given version.
	 * @param ver the version
	 * @return the CookieJar with Cookies; always non-null
	 */
	public CookieJar getVersionCookies(String ver)
	{
		CookieJar cj = new CookieJar();
		Cookie c;

		for(int i=0; i < iNumCookies; i++)
		{
			c = (Cookie) theJar.get(i);
			if(c.getVersion().equals(ver))
			{
				cj.add(c);
			}
		}

		return(cj);
	}

	public String toString()
	{
		if(isEmpty())
		{
			return("{}");
		}

		StringBuffer sb = new StringBuffer();

		sb.append("{");

		for(int i=0; i < iNumCookies; i++)
		{
			sb.append(theJar.get(i).toString());
			sb.append(",");
		}

		sb.deleteCharAt(sb.length()-1);
		sb.append("}");

		return(sb.toString());
	}
}