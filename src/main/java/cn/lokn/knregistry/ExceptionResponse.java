package cn.lokn.knregistry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * @description:
 * @author: lokn
 * @date: 2024/04/21 21:22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionResponse {

    HttpStatus httpStatus;
    String message;
}
