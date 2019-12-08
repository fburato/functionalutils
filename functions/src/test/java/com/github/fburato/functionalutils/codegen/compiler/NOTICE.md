# Authorship notice

The code available in this package is taken from the excellent [InMemoryJavaCompiler](https://github.com/trung/InMemoryJavaCompiler)
by [trung](https://github.com/trung). I've copied the source code directly for two reasons:

- The original library declares a dependency on `slf4j` and I want to avoid introducing any additional dependencies, even if 
  even just for testing.
- I want to have the compiler evolve as new versions of Java are released. Since the project has been dormant for two years,
  I decided to pick it up and include it directly so that I will be able to apply personally changes if required without
  having to bother trung for new releases.

The original project has been released under Apache License 2.0 and I'm maintaing the license. No change in the source code
has been applied, with the exception of the removal of the comments.
