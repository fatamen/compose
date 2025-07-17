package tw.com.ispan.eeit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 這是一個自訂的例外類別，專門用在找不到特定資源時拋出。
 * AT ResponseStatus(HttpStatus.NOT_FOUND) 這個註解會告訴 Spring Boot，
 * 當這個例外被拋出且未被捕獲時，應該自動回傳 HTTP 404 Not Found 狀態碼。
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}