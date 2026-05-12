SHOW DATABASES LIKE 'ccms_db';
SELECT user, host FROM mysql.user WHERE user LIKE '%ccms%' OR user = 'ccms_user';