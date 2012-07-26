package com.github.goldin.plugins.gradle.general.duplicates


/**
 * {@link DuplicatesFinderPlugin} extension object.
 * http://gradle.org/docs/1.0-milestone-7/userguide/custom_plugins.html#N14C69
 */
class DuplicatesFinderExtension
{
    List<String> configurations = null  // Default - all configurations are checked
    boolean      fail           = true  // Whether execution should fail when duplicates are found
    boolean      verbose        = false // Whether logging is verbose
}