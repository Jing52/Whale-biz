package com.whale.framework.process.service;

import com.magus.framework.camunda.api.dto.req.ProcessDefinitionSearchReq;
import com.magus.framework.camunda.api.dto.rsp.ProcessDefinitionRsp;
import com.whale.framework.process.dto.req.ProcessDefinitionTemplateReq;
import com.whale.framework.process.dto.rsp.ProcessDefinitionTemplateRsp;
import com.whale.framework.process.entity.ProcessDefinitionTemplate;
import com.whale.framework.process.enums.ProcessTemplateEnum;
import com.whale.framework.process.repository.ProcessDefinitionTemplateRepository;
import com.whale.framework.process.result.ProcessResultEnum;
import com.magus.framework.core.exception.MagusException;
import com.magus.framework.core.exception.ResultEnum;
import com.magus.framework.core.utils.JsonUtils;
import com.magus.framework.core.utils.LoginUserUtils;
import com.magus.framework.core.utils.MagusUtils;
import com.magus.framework.persistence.JpaSearchUtils;
import com.magus.framework.persistence.SearchFilter;
import com.magus.framework.service.BaseService;
import com.magus.framework.utils.PageUtils;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @ProjectName: magus-engine
 * @See: com.magus.framework.camunda.service
 * @Description: 流程定义模板实现
 * @Author: Whale
 * @Date: 2023/04/27 3:27 PM
 */
@Slf4j
@Service
@GlobalTransactional
public class ProcessDefinitionTemplateService extends BaseService<ProcessDefinitionTemplate, String> {

    @Autowired
    ProcessDefinitionTemplateRepository repository;

    @Autowired
    private RepositoryService repositoryService;

    public PageImpl<ProcessDefinitionRsp> page(ProcessDefinitionSearchReq req) {
        if (Objects.isNull(req) || Objects.isNull(req.getType())
                || StringUtils.isBlank(req.getIndustryCategory()) || StringUtils.isBlank(req.getSceneCategory())) {
            throw new MagusException(ResultEnum.RESULT_ERROR_PARAM);
        }
        List<SearchFilter> searchFilters = new ArrayList<>();

        switch (ProcessTemplateEnum.valueOf(req.getType())) {
            case MINE -> {
                // 我的模板包含【系统】和【我的】类型的模板
                searchFilters.add(SearchFilter.equal(ProcessDefinitionTemplate::getType, ProcessTemplateEnum.MINE.name()));
                searchFilters.add(SearchFilter.equal(ProcessDefinitionTemplate::getCreateId, LoginUserUtils.getLoginId()));
            }
            case SYSTEM -> {
                searchFilters.add(SearchFilter.equal(ProcessDefinitionTemplate::getType, req.getType()));

            }
            case COMPANY -> {
                searchFilters.add(SearchFilter.equal(ProcessDefinitionTemplate::getType, req.getType()));
                searchFilters.add(SearchFilter.equal(ProcessDefinitionTemplate::getCompId, LoginUserUtils.getCompId()));
            }
            default -> {
                throw new MagusException(ResultEnum.RESULT_ERROR_OBJ_NOT_EXIST);
            }
        }
        // 行业
        if (StringUtils.isNotBlank(req.getIndustryCategory())) {
            searchFilters.add(SearchFilter.equal(ProcessDefinitionTemplate::getIndustryCategory, req.getIndustryCategory()));
        }
        // 场景
        if (StringUtils.isNotBlank(req.getSceneCategory())) {
            searchFilters.add(SearchFilter.equal(ProcessDefinitionTemplate::getSceneCategory, req.getSceneCategory()));
        }
        searchFilters.add(SearchFilter.equal(ProcessDefinitionTemplate::getDeleted, false));

        Specification<ProcessDefinitionTemplate> spec = JpaSearchUtils.buildAndSpec(searchFilters);
        PageRequest pageRequest = PageUtils.page(req);
        Page<ProcessDefinitionTemplate> page = this.repository.findAll(spec, pageRequest);

        return new PageImpl<>(MagusUtils.copyList(page.getContent(), ProcessDefinitionRsp.class), pageRequest, page.getTotalElements());
    }

    /**
     * 流程定义详情
     *
     * @param processDefinitionId
     * @return
     */
    public ProcessDefinitionTemplate findByProcDefId(String processDefinitionId) {
        if (StringUtils.isBlank(processDefinitionId)) {
            return null;
        }
        return repository.findByProcDefIdAndDeletedIsFalse(processDefinitionId);
    }

    /**
     * 流程预览
     *
     * @param processDefinitionId
     * @return
     */
    public ProcessDefinitionTemplateRsp preview(String processDefinitionId) {
        BpmnModelInstance modelInstance = this.repositoryService.getBpmnModelInstance(processDefinitionId);
        ProcessDefinitionTemplateRsp modelRsp = new ProcessDefinitionTemplateRsp();
        try {
            String modelStr = Bpmn.convertToString(modelInstance);
            modelRsp.setBpmnModel(modelStr);
        } catch (Exception e) {
            log.error("预览流程图失败,processDefinitionId={}", processDefinitionId);
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROCESS_PARSE);
        }
        return modelRsp;
    }

    /**
     * 模板上传
     *
     * @param req
     * @param file
     * @return
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public ProcessDefinitionTemplate upload(ProcessDefinitionTemplateReq req, MultipartFile file) {
        log.info("=========================> 流程模板上传 入参：{}", JsonUtils.toJson(req));
        BpmnModelInstance modelInstance = null;
        ProcessDefinition definition = null;
        Deployment deploy = null;
        try {
            modelInstance = Bpmn.readModelFromStream(file.getInputStream());
            DeploymentBuilder addModelInstance = repositoryService.createDeployment()
                    .addModelInstance(file.getOriginalFilename(), modelInstance);
            deploy = addModelInstance.deployWithResult();
            log.info("=========================> 流程模板上传 流程部署完成 {}", deploy.getId());
        } catch (Exception e) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROCESS_PARSE, e.getMessage());
        }

        definition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploy.getId()).singleResult();
        log.info("=========================> 流程模板上传 流程部署后流程定义：{}", definition.getId());

        if (Objects.isNull(definition)) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_PROC_DEF_NOT_EXIST);
        }

        // 保存流程定义
        ProcessDefinitionTemplate entity = new ProcessDefinitionTemplate();
        entity.setProcDefId(definition.getId());
        entity.setProcDefName(definition.getName());
        entity.setProcDefKey(definition.getKey());
        entity.setType(req.getType().name());
        entity.setCoverPage(req.getCoverPage());
        entity.setVersion(definition.getVersion());
        entity.setCompId(LoginUserUtils.getCompId());
        ProcessDefinitionTemplate save = this.repository.save(entity);

        log.info("=========================> 流程模板上传 结束：{}", JsonUtils.toJson(save));
        return save;
    }

    /**
     * 流程定义删除
     *
     * @param processDefinitionId
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void deleteById(String processDefinitionId) {
        if (StringUtils.isBlank(processDefinitionId)) {
            return;
        }
        ProcessDefinitionTemplate template = findByProcDefId(processDefinitionId);
        if (Objects.isNull(template)) {
            return;
        }
        if (!StringUtils.equals(template.getCreateId(), LoginUserUtils.getId())) {
            throw new MagusException(ProcessResultEnum.RESULT_ERROR_UNABLE_OPERATE);
        }

        repository.delete(template);
    }
}
