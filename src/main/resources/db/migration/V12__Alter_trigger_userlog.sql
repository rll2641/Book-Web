DROP TRIGGER IF EXISTS user_delete_log;

DELIMITER $$
CREATE TRIGGER user_delete_log
    BEFORE DELETE ON user
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