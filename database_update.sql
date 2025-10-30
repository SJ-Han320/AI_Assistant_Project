-- SparkTask 테이블에 새로운 컬럼들 추가
ALTER TABLE spark_task 
ADD COLUMN st_host VARCHAR(255),
ADD COLUMN st_db VARCHAR(255),
ADD COLUMN st_table VARCHAR(255),
ADD COLUMN st_db_id VARCHAR(255),
ADD COLUMN st_db_pw VARCHAR(255),
ADD COLUMN st_field TEXT;
