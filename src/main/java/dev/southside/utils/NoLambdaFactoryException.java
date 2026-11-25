package dev.southside.utils;

public class NoLambdaFactoryException extends RuntimeException {
   public NoLambdaFactoryException(Class<?> clazz) {
      super("No registered lambda listener for '" + clazz.getName() + "'.");
   }
}
