DELIMITER $$
CREATE TRIGGER user_insert_log
    AFTER INSERT ON user
    FOR EACH ROW
BEGIN
    INSERT INTO `user_log` (`USER_ID`, `ACTION_TYPE`, `BEFORE_DATA`, `AFTER_DATA`)
    VALUES (NEW.USER_ID,
            'INSERT',
            NULL,
            JSON_OBJECT(
                    'USER_ID', NEW.USER_ID,
                    'GRADE_ID', NEW.GRADE_ID,
                    'USER_EMAIL', NEW.USER_EMAIL,
                    'USER_NICKNAME', NEW.USER_NICKNAME,
                    'USER_NAME', NEW.USER_NAME,
                    'USER_PHONE', NEW.USER_PHONE,
                    'USER_STATUS', NEW.USER_STATUS,
                    'POINT', NEW.POINT,
                    'POSTCODE', NEW.POSTCODE,
                    'DEFAULT_ADDRESS', NEW.DEFAULT_ADDRESS,
                    'DETAIL_ADDRESS', NEW.DETAIL_ADDRESS,
                    'CITY', NEW.CITY,
                    'PROVINCE', NEW.PROVINCE
            ));
END$$

CREATE TRIGGER user_update_log
    AFTER UPDATE ON user
    FOR EACH ROW
BEGIN
    INSERT INTO `user_log` (`USER_ID`, `ACTION_TYPE`, `BEFORE_DATA`, `AFTER_DATA`)
    VALUES (NEW.USER_ID,
            'UPDATE',
            JSON_OBJECT(
                    'USER_ID', OLD.USER_ID,
                    'GRADE_ID', OLD.GRADE_ID,
                    'USER_EMAIL', OLD.USER_EMAIL,
                    'USER_NICKNAME', OLD.USER_NICKNAME,
                    'USER_NAME', OLD.USER_NAME,
                    'USER_PHONE', OLD.USER_PHONE,
                    'USER_STATUS', OLD.USER_STATUS,
                    'POINT', OLD.POINT,
                    'POSTCODE', OLD.POSTCODE,
                    'DEFAULT_ADDRESS', OLD.DEFAULT_ADDRESS,
                    'DETAIL_ADDRESS', OLD.DETAIL_ADDRESS,
                    'CITY', OLD.CITY,
                    'PROVINCE', OLD.PROVINCE
            ),
            JSON_OBJECT(
                    'USER_ID', NEW.USER_ID,
                    'GRADE_ID', NEW.GRADE_ID,
                    'USER_EMAIL', NEW.USER_EMAIL,
                    'USER_NICKNAME', NEW.USER_NICKNAME,
                    'USER_NAME', NEW.USER_NAME,
                    'USER_PHONE', NEW.USER_PHONE,
                    'USER_STATUS', NEW.USER_STATUS,
                    'POINT', NEW.POINT,
                    'POSTCODE', NEW.POSTCODE,
                    'DEFAULT_ADDRESS', NEW.DEFAULT_ADDRESS,
                    'DETAIL_ADDRESS', NEW.DETAIL_ADDRESS,
                    'CITY', NEW.CITY,
                    'PROVINCE', NEW.PROVINCE
            ));
END$$

CREATE TRIGGER user_delete_log
    AFTER DELETE ON user
    FOR EACH ROW
BEGIN
    INSERT INTO `user_log` (`USER_ID`, `ACTION_TYPE`, `BEFORE_DATA`, `AFTER_DATA`)
    VALUES (OLD.USER_ID,
            'DELETE',
            JSON_OBJECT(
                    'USER_ID', OLD.USER_ID,
                    'GRADE_ID', OLD.GRADE_ID,
                    'USER_EMAIL', OLD.USER_EMAIL,
                    'USER_NICKNAME', OLD.USER_NICKNAME,
                    'USER_NAME', OLD.USER_NAME,
                    'USER_PHONE', OLD.USER_PHONE,
                    'USER_STATUS', OLD.USER_STATUS,
                    'POINT', OLD.POINT,
                    'POSTCODE', OLD.POSTCODE,
                    'DEFAULT_ADDRESS', OLD.DEFAULT_ADDRESS,
                    'DETAIL_ADDRESS', OLD.DETAIL_ADDRESS,
                    'CITY', OLD.CITY,
                    'PROVINCE', OLD.PROVINCE
            ),
            NULL);
END$$

DELIMITER ;