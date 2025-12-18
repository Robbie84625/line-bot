package com.robbie.linebot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.robbie.linebot.constant.ErrorInfo;
import java.text.MessageFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;

/** Result Error API預期的例外錯誤 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultError implements ErrorInfo {

  /** 錯誤代碼 */
  @JsonProperty(value = "errorCode")
  private String errorCode;

  /** 錯誤訊息 */
  @JsonProperty(value = "errorMessage")
  private String errorMessage;

  /** constructor */
  public ResultError(ErrorInfo error, String... msgs) {
    String errorMsg = error.getErrorMessage();
    if (ArrayUtils.isNotEmpty(msgs)) {
      MessageFormat fmt = new MessageFormat(errorMsg);
      errorMsg = fmt.format(msgs);
    }
    this.errorCode = error.getErrorCode();
    this.errorMessage = errorMsg;
  }
}
