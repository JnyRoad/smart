-- OA 回调报文审计表：每次 /oa/workflow/over 回调先落库再分发（spec §3.3）
create table oa_callback_log (
    id                 number(19)     primary key,
    request_id         varchar2(64)   not null,           -- OA requestid
    payload            clob,                              -- 完整回调报文 JSON
    receive_time       date           not null,
    status             number(1)      default 0 not null, -- 0=已接收 1=处理成功 2=部分失败
    resolved           number(1)      default 0 not null, -- 0=未解决 1=已解决
    succeeded_handlers varchar2(512),                     -- 成功 handler 名逗号分隔（跳过集合，含合并值）
    failed_handlers    varchar2(512),                     -- 失败 handler 名逗号分隔
    last_error         varchar2(2000),                    -- 最后一次失败摘要
    retry_count        number(3)      default 0 not null, -- 重放次数
    cost_ms            number(10)                         -- 分发耗时毫秒
);

comment on table oa_callback_log is 'OA工作流回调审计与重放日志';

-- 未解决 partial 查询 + 排序支撑索引（spec §3.3）
create index idx_oa_cb_req on oa_callback_log (request_id, status, resolved, receive_time, id);

-- 不变量兜底：任一 request_id 至多一条未解决 partial（仅兜底日志层不变量，不防副作用重复，spec §3.2.2）
create unique index ux_oa_cb_unresolved
    on oa_callback_log (case when status = 2 and resolved = 0 then request_id end);
