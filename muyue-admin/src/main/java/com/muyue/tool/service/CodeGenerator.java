package com.muyue.tool.service;

import com.muyue.tool.domain.GenColumn;
import com.muyue.tool.domain.GenTable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 代码生成器 - 按项目约定生成 Entity / Mapper / Service / Controller / 前端页面
 *
 * 生成物均为源码文本（预览 + zip 下载），由开发者按需放入工程对应包路径。
 *
 * @author muyue
 */
@Service
public class CodeGenerator {

    /** BaseEntity 已包含的列（生成实体时跳过） */
    private static final Set<String> BASE_COLUMNS = Set.of(
            "create_by", "create_time", "update_by", "update_time", "remark");

    /**
     * 生成某个表的全部代码文件
     *
     * @return key = 文件展示名，value = 文件内容
     */
    public Map<String, String> generate(GenTable table) {
        List<GenColumn> columns = table.getColumns();
        boolean hasBase = columns.stream().anyMatch(c -> "create_by".equals(c.getColumnName()))
                && columns.stream().anyMatch(c -> "create_time".equals(c.getColumnName()));

        Map<String, String> files = new LinkedHashMap<>();
        files.put(table.getClassName() + ".java", entity(table, hasBase));
        files.put(table.getClassName() + "Mapper.java", mapper(table));
        files.put(table.getClassName() + "Mapper.xml", mapperXml(table));
        files.put("I" + table.getClassName() + "Service.java", service(table));
        files.put(table.getClassName() + "ServiceImpl.java", serviceImpl(table));
        files.put(table.getClassName() + "Controller.java", controller(table));
        files.put(table.getBusinessName() + ".js", apiJs(table));
        files.put("index.vue", vuePage(table));
        files.put("menu.sql", menuSql(table));
        return files;
    }

    // ==================== 实体 ====================
    private String entity(GenTable table, boolean hasBase) {
        String pkg = "com.muyue." + table.getModuleName() + ".domain";
        StringBuilder fields = new StringBuilder();
        List<String> imports = new ArrayList<>();
        imports.add("com.baomidou.mybatisplus.annotation.IdType");
        imports.add("com.baomidou.mybatisplus.annotation.TableId");
        imports.add("com.baomidou.mybatisplus.annotation.TableName");
        imports.add("lombok.Data");
        if (hasBase) {
            imports.add("com.muyue.common.core.domain.BaseEntity");
            imports.add("lombok.EqualsAndHashCode");
        }
        for (GenColumn column : table.getColumns()) {
            if (hasBase && BASE_COLUMNS.contains(column.getColumnName())) {
                continue;
            }
            if ("BigDecimal".equals(column.getJavaType())) {
                imports.add("java.math.BigDecimal");
            }
            if ("LocalDateTime".equals(column.getJavaType())) {
                imports.add("java.time.LocalDateTime");
            }
            String comment = column.getColumnComment().isBlank() ? column.getColumnName() : column.getColumnComment();
            fields.append("\n    /** ").append(comment).append(" */\n");
            if ("1".equals(column.getIsPk())) {
                fields.append("    @TableId(type = IdType.ASSIGN_ID)\n");
            }
            fields.append("    private ").append(column.getJavaType()).append(" ").append(column.getJavaField()).append(";\n");
        }

        StringBuilder importBlock = new StringBuilder();
        imports.stream().distinct().sorted().forEach(i -> importBlock.append("import ").append(i).append(";\n"));

        String classDecl = hasBase
                ? "public class " + table.getClassName() + " extends BaseEntity {"
                : "public class " + table.getClassName() + " implements java.io.Serializable {\n\n    private static final long serialVersionUID = 1L;";
        String lombok = hasBase
                ? "@Data\n@EqualsAndHashCode(callSuper = true)"
                : "@Data";

        return """
                package %PKG%;

                %IMPORTS%
                /**
                 * %FUNC% 实体（表 %TABLE%）
                 *
                 * @author muyue
                 */
                @TableName("%TABLE%")
                %LOMBOK%
                %CLASS%

                %FIELDS%}
                """
                .replace("%PKG%", pkg)
                .replace("%IMPORTS%", importBlock.toString())
                .replace("%FUNC%", table.getFunctionName())
                .replace("%TABLE%", table.getTableName())
                .replace("%LOMBOK%", lombok)
                .replace("%CLASS%", classDecl)
                .replace("%FIELDS%", fields.toString());
    }

    // ==================== Mapper ====================
    private String mapper(GenTable table) {
        String entity = "com.muyue." + table.getModuleName() + ".domain." + table.getClassName();
        return """
                package com.muyue.%MODULE%.mapper;

                import com.baomidou.mybatisplus.core.mapper.BaseMapper;
                import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
                import %ENTITY%;
                import org.apache.ibatis.annotations.Param;

                import java.util.List;

                /**
                 * %FUNC% 数据访问层
                 *
                 * @author muyue
                 */
                public interface %CLASS%Mapper extends BaseMapper<%CLASS%> {

                    List<%CLASS%> select%CLASS%List(Page<%CLASS%> page, @Param("query") %CLASS% query);
                }
                """
                .replace("%MODULE%", table.getModuleName())
                .replace("%ENTITY%", entity)
                .replace("%FUNC%", table.getFunctionName())
                .replace("%CLASS%", table.getClassName());
    }

    private String mapperXml(GenTable table) {
        String entity = "com.muyue." + table.getModuleName() + ".domain." + table.getClassName();
        StringBuilder columns = new StringBuilder();
        StringBuilder conditions = new StringBuilder();
        for (GenColumn column : table.getColumns()) {
            columns.append("t.").append(column.getColumnName()).append(", ");
            if ("1".equals(column.getIsPk()) || BASE_COLUMNS.contains(column.getColumnName())) {
                continue;
            }
            if ("String".equals(column.getJavaType())) {
                conditions.append("""
                                    <if test="query.%FIELD% != null and query.%FIELD% != ''">
                                        AND t.%COL% LIKE '%' || #{query.%FIELD%} || '%'
                                    </if>
                                    """
                        .replace("%FIELD%", column.getJavaField())
                        .replace("%COL%", column.getColumnName()));
            } else {
                conditions.append("""
                                    <if test="query.%FIELD% != null">
                                        AND t.%COL% = #{query.%FIELD%}
                                    </if>
                                    """
                        .replace("%FIELD%", column.getJavaField())
                        .replace("%COL%", column.getColumnName()));
            }
        }
        String columnList = columns.length() > 2 ? columns.substring(0, columns.length() - 2) : columns.toString();
        String orderBy = table.getPkColumn() != null ? "t." + table.getPkColumn().getColumnName() : "t.create_time";
        return """
                <?xml version="1.0" encoding="UTF-8" ?>
                <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
                <mapper namespace="com.muyue.%MODULE%.mapper.%CLASS%Mapper">

                    <select id="select%CLASS%List" resultType="%ENTITY%">
                        SELECT %COLUMNS%
                        FROM %TABLE% t
                        <where>
                            1 = 1
                            %CONDITIONS%
                        </where>
                        ORDER BY %ORDER% DESC
                    </select>
                </mapper>
                """
                .replace("%MODULE%", table.getModuleName())
                .replace("%CLASS%", table.getClassName())
                .replace("%ENTITY%", entity)
                .replace("%COLUMNS%", columnList)
                .replace("%TABLE%", table.getTableName())
                .replace("%CONDITIONS%", conditions.toString().stripTrailing())
                .replace("%ORDER%", orderBy);
    }

    // ==================== Service ====================
    private String service(GenTable table) {
        String entity = "com.muyue." + table.getModuleName() + ".domain." + table.getClassName();
        return """
                package com.muyue.%MODULE%.service;

                import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
                import com.baomidou.mybatisplus.extension.service.IService;
                import %ENTITY%;

                /**
                 * %FUNC% 服务接口
                 *
                 * @author muyue
                 */
                public interface I%CLASS%Service extends IService<%CLASS%> {

                    /** 分页查询 */
                    Page<%CLASS%> selectPage(Page<%CLASS%> page, %CLASS% query);
                }
                """
                .replace("%MODULE%", table.getModuleName())
                .replace("%ENTITY%", entity)
                .replace("%FUNC%", table.getFunctionName())
                .replace("%CLASS%", table.getClassName());
    }

    private String serviceImpl(GenTable table) {
        String entity = "com.muyue." + table.getModuleName() + ".domain." + table.getClassName();
        String mapper = "com.muyue." + table.getModuleName() + ".mapper." + table.getClassName() + "Mapper";
        return """
                package com.muyue.%MODULE%.service.impl;

                import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
                import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
                import %ENTITY%;
                import %MAPPER%;
                import com.muyue.%MODULE%.service.I%CLASS%Service;
                import lombok.RequiredArgsConstructor;
                import org.springframework.stereotype.Service;

                import java.util.List;

                /**
                 * %FUNC% 服务实现
                 *
                 * @author muyue
                 */
                @Service
                @RequiredArgsConstructor
                public class %CLASS%ServiceImpl extends ServiceImpl<%CLASS%Mapper, %CLASS%> implements I%CLASS%Service {

                    @Override
                    public Page<%CLASS%> selectPage(Page<%CLASS%> page, %CLASS% query) {
                        List<%CLASS%> list = baseMapper.select%CLASS%List(page, query);
                        page.setRecords(list);
                        return page;
                    }
                }
                """
                .replace("%MODULE%", table.getModuleName())
                .replace("%ENTITY%", entity)
                .replace("%MAPPER%", mapper)
                .replace("%FUNC%", table.getFunctionName())
                .replace("%CLASS%", table.getClassName());
    }

    // ==================== Controller ====================
    private String controller(GenTable table) {
        String entity = "com.muyue." + table.getModuleName() + ".domain." + table.getClassName();
        String service = "com.muyue." + table.getModuleName() + ".service.I" + table.getClassName() + "Service";
        String perms = table.getModuleName() + ":" + table.getBusinessName();
        String pkType = table.getPkColumn() != null ? table.getPkColumn().getJavaType() : "Long";
        String pkField = table.getPkColumn() != null ? table.getPkColumn().getJavaField() : "id";
        return """
                package com.muyue.web.controller.%MODULE%;

                import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
                import com.muyue.common.annotation.Log;
                import com.muyue.common.core.controller.BaseController;
                import com.muyue.common.core.domain.R;
                import com.muyue.common.core.domain.TableDataInfo;
                import com.muyue.common.enums.BusinessType;
                import %ENTITY%;
                import %SERVICE%;
                import lombok.RequiredArgsConstructor;
                import org.springframework.security.access.prepost.PreAuthorize;
                import org.springframework.web.bind.annotation.DeleteMapping;
                import org.springframework.web.bind.annotation.GetMapping;
                import org.springframework.web.bind.annotation.PathVariable;
                import org.springframework.web.bind.annotation.PostMapping;
                import org.springframework.web.bind.annotation.PutMapping;
                import org.springframework.web.bind.annotation.RequestBody;
                import org.springframework.web.bind.annotation.RequestMapping;
                import org.springframework.web.bind.annotation.RestController;

                import java.util.Arrays;
                import java.util.List;
                import java.util.stream.Collectors;

                /**
                 * %FUNC% 控制器
                 *
                 * @author muyue
                 */
                @RestController
                @RequestMapping("/%MODULE%/%BUSINESS%")
                @RequiredArgsConstructor
                public class %CLASS%Controller extends BaseController {

                    private final I%CLASS%Service %FIELD%Service;

                    /** 列表（分页） */
                    @PreAuthorize("@ps.hasPermi('%PERMS%:list')")
                    @GetMapping("/list")
                    public TableDataInfo<%CLASS%> list(%CLASS% query) {
                        Page<%CLASS%> page = %FIELD%Service.selectPage(getPage(), query);
                        return new TableDataInfo<>(page.getRecords(), page.getTotal());
                    }

                    /** 详情 */
                    @PreAuthorize("@ps.hasPermi('%PERMS%:query')")
                    @GetMapping("/{%FIELD%}")
                    public R<%CLASS%> getInfo(@PathVariable %PKTYPE% %FIELD%) {
                        return R.ok(%FIELD%Service.getById(%FIELD%));
                    }

                    /** 新增 */
                    @Log(title = "%FUNC%", businessType = BusinessType.INSERT)
                    @PreAuthorize("@ps.hasPermi('%PERMS%:add')")
                    @PostMapping
                    public R<Void> add(@RequestBody %CLASS% %FIELD%) {
                        return toAjax(%FIELD%Service.save(%FIELD%));
                    }

                    /** 修改 */
                    @Log(title = "%FUNC%", businessType = BusinessType.UPDATE)
                    @PreAuthorize("@ps.hasPermi('%PERMS%:edit')")
                    @PutMapping
                    public R<Void> edit(@RequestBody %CLASS% %FIELD%) {
                        return toAjax(%FIELD%Service.updateById(%FIELD%));
                    }

                    /** 删除 */
                    @Log(title = "%FUNC%", businessType = BusinessType.DELETE)
                    @PreAuthorize("@ps.hasPermi('%PERMS%:remove')")
                    @DeleteMapping("/{%FIELD%}Ids")
                    public R<Void> remove(@PathVariable String %FIELD%Ids) {
                        List<%PKTYPE%> ids = Arrays.stream(%FIELD%Ids.split(","))
                                .map(%PKTYPE%::valueOf)
                                .collect(Collectors.toList());
                        return toAjax(%FIELD%Service.removeByIds(ids));
                    }
                }
                """
                .replace("%MODULE%", table.getModuleName())
                .replace("%ENTITY%", entity)
                .replace("%SERVICE%", service)
                .replace("%CLASS%", table.getClassName())
                .replace("%FIELD%", pkField)
                .replace("%PERMS%", perms)
                .replace("%PKTYPE%", pkType)
                .replace("%BUSINESS%", table.getBusinessName())
                .replace("%FUNC%", table.getFunctionName());
    }

    // ==================== 前端 api ====================
    private String apiJs(GenTable table) {
        String url = "/" + table.getModuleName() + "/" + table.getBusinessName();
        return """
                import request from '@/utils/request'

                // 列表
                export function list(query) {
                  return request({ url: '%URL%/list', method: 'get', params: query })
                }
                // 详情
                export function get(id) {
                  return request({ url: '%URL%/' + id, method: 'get' })
                }
                // 新增
                export function add(data) {
                  return request({ url: '%URL%', method: 'post', data })
                }
                // 修改
                export function update(data) {
                  return request({ url: '%URL%', method: 'put', data })
                }
                // 删除
                export function del(id) {
                  return request({ url: '%URL%/' + id, method: 'delete' })
                }
                """.replace("%URL%", url);
    }

    // ==================== 前端页面 ====================
    private String vuePage(GenTable table) {
        StringBuilder columnDefs = new StringBuilder();
        StringBuilder tableColumns = new StringBuilder();
        List<GenColumn> fields = table.getColumns().stream()
                .filter(c -> !BASE_COLUMNS.contains(c.getColumnName()))
                .toList();
        for (GenColumn column : fields) {
            String label = column.getColumnComment().isBlank() ? column.getJavaField() : column.getColumnComment();
            boolean search = "String".equals(column.getJavaType()) && !"1".equals(column.getIsPk());
            columnDefs.append("        { prop: '").append(column.getJavaField()).append("', label: '")
                    .append(label).append("', pk: ").append("1".equals(column.getIsPk()))
                    .append(", search: ").append(search).append(" },\n");
            tableColumns.append("      <el-table-column v-for=\"col in columns\" :key=\"col.prop\" :label=\"col.label\" :prop=\"col.prop\" />\n");
        }
        return """
                <template>
                  <div class="app-container">
                    <el-form :model="queryParams" :inline="true" class="search-form">
                      <el-form-item v-for="field in queryFields" :key="field.prop" :label="field.label">
                        <el-input v-model="queryParams[field.prop]" :placeholder="'请输入' + field.label" clearable @keyup.enter="handleQuery" />
                      </el-form-item>
                      <el-form-item>
                        <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
                        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
                      </el-form-item>
                    </el-form>

                    <el-row :gutter="10" class="table-toolbar">
                      <el-col :span="1.5">
                        <el-button type="primary" plain :icon="Plus" @click="handleAdd">新增</el-button>
                      </el-col>
                      <el-col :span="1.5">
                        <el-button type="danger" plain :icon="Delete" :disabled="!selectedIds.length" @click="handleDelete">删除</el-button>
                      </el-col>
                    </el-row>

                    <el-table :data="list" v-loading="loading" border @selection-change="handleSelectionChange" class="page-container">
                      <el-table-column type="selection" width="50" align="center" />
                %TABLE_COLUMNS%
                      <el-table-column label="操作" width="160" align="center">
                        <template #default="{ row }">
                          <el-button link type="primary" :icon="Edit" @click="handleUpdate(row)">修改</el-button>
                          <el-button link type="primary" :icon="Delete" @click="handleDelete(row)">删除</el-button>
                        </template>
                      </el-table-column>
                    </el-table>

                    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

                    <el-dialog :title="title" v-model="open" width="560px" append-to-body>
                      <el-form :model="form" ref="formRef" label-width="100px">
                        <el-form-item v-for="field in formFields" :key="field.prop" :label="field.label">
                          <el-input v-model="form[field.prop]" :placeholder="'请输入' + field.label" />
                        </el-form-item>
                      </el-form>
                      <template #footer>
                        <el-button @click="open = false">取 消</el-button>
                        <el-button type="primary" @click="submitForm">确 定</el-button>
                      </template>
                    </el-dialog>
                  </div>
                </template>

                <script setup>
                import { onMounted, reactive, ref } from 'vue'
                import { ElMessage, ElMessageBox } from 'element-plus'
                import { Search, Refresh, Plus, Delete, Edit } from '@element-plus/icons-vue'
                import { list, get, add, update, del } from '@/api/%MODULE%/%BUSINESS%'

                const loading = ref(false)
                const list = ref([])
                const total = ref(0)
                const open = ref(false)
                const title = ref('')
                const selectedIds = ref([])
                const formRef = ref()

                const columns = [
                %COLUMN_DEFS%        { prop: 'createTime', label: '创建时间' }
                ]
                const queryFields = columns.filter((c) => c.search).slice(0, 3)
                const formFields = columns.filter((c) => !c.pk && c.prop !== 'createTime')

                const queryParams = reactive({ pageNum: 1, pageSize: 10 })
                const form = reactive({})

                function getList() {
                  loading.value = true
                  list(queryParams).then((res) => {
                    list.value = res.rows
                    total.value = res.total
                    loading.value = false
                  })
                }
                function handleQuery() {
                  queryParams.pageNum = 1
                  getList()
                }
                function resetQuery() {
                  queryFields.forEach((f) => (queryParams[f.prop] = ''))
                  handleQuery()
                }
                function handleSelectionChange(rows) {
                  selectedIds.value = rows.map((r) => r[columns.find((c) => c.pk).prop])
                }
                function handleAdd() {
                  formFields.forEach((f) => (form[f.prop] = ''))
                  form[columns.find((c) => c.pk).prop] = undefined
                  open.value = true
                  title.value = '新增'
                }
                function handleUpdate(row) {
                  const id = row[columns.find((c) => c.pk).prop]
                  get(id).then((res) => {
                    Object.keys(form).forEach((k) => delete form[k])
                    Object.assign(form, res.data)
                    open.value = true
                    title.value = '修改'
                  })
                }
                function submitForm() {
                  const id = form[columns.find((c) => c.pk).prop]
                  const action = id ? update(form) : add(form)
                  action.then(() => {
                    ElMessage.success(id ? '修改成功' : '新增成功')
                    open.value = false
                    getList()
                  })
                }
                function handleDelete(row) {
                  const pkProp = columns.find((c) => c.pk).prop
                  const ids = row[pkProp] ? [row[pkProp]] : selectedIds.value
                  ElMessageBox.confirm('是否确认删除选中的数据项？', '提示', { type: 'warning' })
                    .then(() => del(ids.join(',')))
                    .then(() => {
                      ElMessage.success('删除成功')
                      getList()
                    })
                }

                onMounted(getList)
                </script>

                <style scoped>
                .app-container {
                  padding: 18px;
                }
                </style>
                """
                .replace("%TABLE_COLUMNS%", tableColumns.toString())
                .replace("%MODULE%", table.getModuleName())
                .replace("%BUSINESS%", table.getBusinessName())
                .replace("%COLUMN_DEFS%", columnDefs.toString());
    }

    // ==================== 菜单 SQL ====================
    private String menuSql(GenTable table) {
        String perms = table.getModuleName() + ":" + table.getBusinessName();
        return """
                -- %FUNC% 菜单与按钮（menu_id 请按实际库自行调整）
                INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
                VALUES (0, '%FUNC%', 1, 20, '%BUSINESS%', '%MODULE%/%BUSINESS%/index', '1', '0', 'C', '0', '0', '%PERMS%:list', 'Document', 'admin');

                INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
                VALUES (0, '%FUNC%查询', 0, 1, '', NULL, '1', '0', 'F', '0', '0', '%PERMS%:query', '#', 'admin');
                INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
                VALUES (0, '%FUNC%新增', 0, 2, '', NULL, '1', '0', 'F', '0', '0', '%PERMS%:add', '#', 'admin');
                INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
                VALUES (0, '%FUNC%修改', 0, 3, '', NULL, '1', '0', 'F', '0', '0', '%PERMS%:edit', '#', 'admin');
                INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by)
                VALUES (0, '%FUNC%删除', 0, 4, '', NULL, '1', '0', 'F', '0', '0', '%PERMS%:remove', '#', 'admin');
                """
                .replace("%FUNC%", table.getFunctionName())
                .replace("%BUSINESS%", table.getBusinessName())
                .replace("%MODULE%", table.getModuleName())
                .replace("%PERMS%", perms);
    }
}
