-- =========================================================================
-- CREATED_AT を UTC オフセット付きで保存できる型へ変更する。
-- Oracle XE 21c および H2(MODE=Oracle) の双方で動く DDL。
-- =========================================================================

ALTER TABLE posts MODIFY (created_at TIMESTAMP(6) WITH TIME ZONE);
