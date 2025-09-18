package util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface Result<T> permits Result.Success, Result.Failure {

	public interface ThrowingSupplier<T> {
	
		T get() throws Exception;
	}

	public static <T> Result<T> of(ThrowingSupplier<T> throwingSupplier) {
		try {
			return new Success<T>(throwingSupplier.get());

		} catch (Exception e) {

			return new Failure<>(e);
		}
	}

	boolean isSuccess();
    boolean isFailure();

    T orElse(T fallback);
    T orElseGet(Supplier<T> supplier);
    T orElseThrow() throws Exception;
    T expect(String message);
    T expect();

    Optional<T> asOptional();

    void ifSuccess(Consumer<T> action);
    void ifFailure(Consumer<Exception> action);

    <R> Result<R> andThen(Function<T, Result<R>> then);

    record Success<T>(T value) implements Result<T>{
	
		public boolean isSuccess() { 

			return true; 
		}

	   	public boolean isFailure() { 

	   		return false; 
	   	}

	   	public T orElse(T fallback) { 

	   		return value; 
	   	}

	   	public T orElseGet(Supplier<T> supplier) { 

	   		return value; 
	   	}

	   	public T orElseThrow() { 

	   		return value; 
	   	}

	   	public T expect() throws UnexpectedResultException {

	   		return value;
	   	}

	   	public T expect(String message) throws UnexpectedResultException { 

	   		return value; 
	   	}

	   	public Optional<T> asOptional() { 

	   		return java.util.Optional.of(value); 
	   	}

	   	public void ifSuccess(Consumer<T> action) {

	   		action.accept(value); 
	   	}
	   	
	   	public void ifFailure(Consumer<Exception> action) {}

	   	public <R> Result<R> andThen(Function<T, Result<R>> then) {

	   		return then.apply(this.value);
	   	}
	}

	record Failure<T>(Exception exception) implements Result<T>{

		public boolean isSuccess() { 

			return false; 
		}

	   	public boolean isFailure() { 

	   		return true; 
	   	}

	   	public T orElse(T fallback) { 

	   		return fallback; 
	   	}

	   	public T orElseGet(Supplier<T> supplier) { 

	   		return supplier.get(); 
	   	}

	   	public T orElseThrow() throws Exception { 

	   		throw exception; 
	   	}

	   	public T expect(String message) throws UnexpectedResultException  { 

	   		throw new UnexpectedResultException(message, exception); 
	   	}

	   	public T expect() throws UnexpectedResultException  { 

	   		throw new UnexpectedResultException("Result did not contain the expected value.", exception); 
	   	}

	   	public Optional<T> asOptional() {

	   		return java.util.Optional.empty(); 
	   	}

	   	public void ifSuccess(Consumer<T> action) {}

	   	public void ifFailure(Consumer<Exception> action) { 

	   		action.accept(exception); 
	   	}

	   	public <R> Result<R> andThen(Function<T, Result<R>> then) {

	   		return new Result.Failure<>(exception);
	   	}
	}
}