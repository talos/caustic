package net.microscraper.execution;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import net.microscraper.client.EncodedNameValuePair;
import net.microscraper.client.Interfaces;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.MustacheTemplateException;
import net.microscraper.client.UnencodedNameValuePair;
import net.microscraper.model.MustacheNameValuePair;
import net.microscraper.model.MustacheTemplate;
import net.microscraper.model.Pattern;

public interface MustacheCompiler {
	/**
	 * Compile a Mustache template.
	 * @param template The {@link MustacheTemplate} to compile.
	 * @return A string with substitutions made.
	 * @throws MissingVariableException if one of the tags could not be resolved.
	 * @throws MustacheTemplateException if the template is invalid.
	 */
	public String compile(MustacheTemplate template) throws MissingVariableException, MustacheTemplateException;

	/**
	 * Compile a Pattern model.
	 * @param uncompiledPattern The {@link Pattern} to compile.
	 * @return {@link Interfaces.Regexp.Pattern} with substitutions made.
	 * @throws MissingVariableException if one of the tags could not be resolved.
	 * @throws MustacheTemplateException if the template is invalid.
	 */
	public Interfaces.Regexp.Pattern compile(Pattern uncompiledPattern) throws MissingVariableException, MustacheTemplateException;
	
	public java.net.URL compile(net.microscraper.model.URL url) throws MalformedURLException, MissingVariableException, MustacheTemplateException;
	
	public Interfaces.Regexp.Pattern[] compile(Pattern[] uncompiledPatterns) throws MissingVariableException, MustacheTemplateException;
	
	public EncodedNameValuePair[] compileEncoded(MustacheNameValuePair[] nameValuePairs)
				throws MissingVariableException, UnsupportedEncodingException, MustacheTemplateException;
	
	public UnencodedNameValuePair[] compileUnencoded(
			MustacheNameValuePair[] nameValuePairs) throws MissingVariableException, MustacheTemplateException;
	
}
