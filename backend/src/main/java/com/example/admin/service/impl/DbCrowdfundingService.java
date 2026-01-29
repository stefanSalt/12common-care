package com.example.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.common.PageResult;
import com.example.admin.dto.crowdfunding.CreateCrowdfundingDonationRequest;
import com.example.admin.dto.crowdfunding.CreateCrowdfundingProjectRequest;
import com.example.admin.dto.crowdfunding.CrowdfundingDonationDto;
import com.example.admin.dto.crowdfunding.CrowdfundingDonationRecordDto;
import com.example.admin.dto.crowdfunding.CrowdfundingProjectDetailDto;
import com.example.admin.dto.crowdfunding.CrowdfundingProjectDto;
import com.example.admin.dto.crowdfunding.CrowdfundingPublicDetailDto;
import com.example.admin.dto.crowdfunding.ManageCrowdfundingProjectRequest;
import com.example.admin.dto.crowdfunding.ReviewCrowdfundingProjectRequest;
import com.example.admin.dto.crowdfunding.UpdateCrowdfundingProjectRequest;
import com.example.admin.entity.BizCrowdfundingDonation;
import com.example.admin.entity.BizCrowdfundingProject;
import com.example.admin.entity.SysFile;
import com.example.admin.entity.SysUser;
import com.example.admin.exception.BusinessException;
import com.example.admin.mapper.BizCrowdfundingDonationMapper;
import com.example.admin.mapper.BizCrowdfundingProjectMapper;
import com.example.admin.mapper.SysFileMapper;
import com.example.admin.mapper.SysUserMapper;
import com.example.admin.service.CrowdfundingService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DbCrowdfundingService implements CrowdfundingService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    private static final Pattern FILE_DOWNLOAD_ID_PATTERN = Pattern.compile("/api/files/(\\d+)/download");

    private final BizCrowdfundingProjectMapper projectMapper;
    private final BizCrowdfundingDonationMapper donationMapper;
    private final SysFileMapper fileMapper;
    private final SysUserMapper userMapper;

    public DbCrowdfundingService(
            BizCrowdfundingProjectMapper projectMapper,
            BizCrowdfundingDonationMapper donationMapper,
            SysFileMapper fileMapper,
            SysUserMapper userMapper
    ) {
        this.projectMapper = projectMapper;
        this.donationMapper = donationMapper;
        this.fileMapper = fileMapper;
        this.userMapper = userMapper;
    }

    @Override
    public PageResult<CrowdfundingProjectDto> listPublic(long current, long size) {
        Page<BizCrowdfundingProject> page = projectMapper.selectPage(
                new Page<>(current, size),
                Wrappers.lambdaQuery(BizCrowdfundingProject.class)
                        .eq(BizCrowdfundingProject::getEnabled, 1)
                        .eq(BizCrowdfundingProject::getStatus, STATUS_APPROVED)
                        .orderByDesc(BizCrowdfundingProject::getId)
        );
        return toPageResult(page.getRecords(), page);
    }

    @Override
    public CrowdfundingPublicDetailDto publicDetail(Long id) {
        if (id == null) {
            throw new BusinessException(400, "id不能为空");
        }

        BizCrowdfundingProject project = projectMapper.selectById(id);
        if (project == null
                || normalizeEnabledFlag(project.getEnabled()) != 1
                || !STATUS_APPROVED.equalsIgnoreCase(trimToNull(project.getStatus()))) {
            throw new BusinessException(404, "众筹项目不存在");
        }

        Page<BizCrowdfundingDonation> donationPage = donationMapper.selectPage(
                new Page<>(1, 10),
                Wrappers.lambdaQuery(BizCrowdfundingDonation.class)
                        .eq(BizCrowdfundingDonation::getProjectId, id)
                        .orderByDesc(BizCrowdfundingDonation::getCreatedAt)
                        .orderByDesc(BizCrowdfundingDonation::getId)
        );

        Map<Long, SysUser> userCache = new HashMap<>();
        List<CrowdfundingDonationDto> donationDtos = donationPage.getRecords().stream()
                .map(d -> toDonationDto(d, userCache))
                .toList();

        CrowdfundingPublicDetailDto dto = new CrowdfundingPublicDetailDto();
        dto.setProject(toDetailDto(project));
        dto.setLatestDonations(donationDtos);
        return dto;
    }

    @Override
    public PageResult<CrowdfundingProjectDto> listAll(long current, long size) {
        Page<BizCrowdfundingProject> page = projectMapper.selectPage(
                new Page<>(current, size),
                Wrappers.lambdaQuery(BizCrowdfundingProject.class)
                        .orderByDesc(BizCrowdfundingProject::getId)
        );
        return toPageResult(page.getRecords(), page);
    }

    @Override
    public CrowdfundingProjectDetailDto getDetail(Long id) {
        if (id == null) {
            throw new BusinessException(400, "id不能为空");
        }
        BizCrowdfundingProject project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(404, "众筹项目不存在");
        }
        return toDetailDto(project);
    }

    @Override
    @Transactional
    public CrowdfundingProjectDetailDto manageUpdate(Long adminUserId, Long id, ManageCrowdfundingProjectRequest request) {
        if (adminUserId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        if (id == null) {
            throw new BusinessException(400, "id不能为空");
        }
        if (request == null) {
            throw new BusinessException(400, "请求不能为空");
        }

        BizCrowdfundingProject existing = projectMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "众筹项目不存在");
        }

        BizCrowdfundingProject update = new BizCrowdfundingProject();
        update.setId(id);

        if (request.getTitle() != null) {
            update.setTitle(requireTrimmed(request.getTitle(), "title不能为空"));
        }
        if (request.getCoverFileId() != null) {
            validatePublicFile(request.getCoverFileId(), "coverFileId不能为空");
            update.setCoverFileId(request.getCoverFileId());
        }
        if (request.getContent() != null) {
            String content = requireTrimmed(request.getContent(), "content不能为空");
            validatePublicImagesInHtml(content);
            update.setContent(content);
        }
        if (request.getTargetAmount() != null) {
            BigDecimal ta = request.getTargetAmount();
            if (ta.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(400, "targetAmount必须大于0");
            }
            update.setTargetAmount(ta);
        }
        if (request.getEndTime() != null) {
            update.setEndTime(request.getEndTime().withNano(0));
        }
        if (request.getEnabled() != null) {
            update.setEnabled(normalizeEnabledFlag(request.getEnabled()));
        }

        projectMapper.updateById(update);

        BizCrowdfundingProject latest = projectMapper.selectById(id);
        return toDetailDto(latest);
    }

    @Override
    @Transactional
    public void delete(Long adminUserId, Long id) {
        if (adminUserId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        if (id == null) {
            throw new BusinessException(400, "id不能为空");
        }

        BizCrowdfundingProject existing = projectMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "众筹项目不存在");
        }

        Long donationCount = donationMapper.selectCount(
                Wrappers.lambdaQuery(BizCrowdfundingDonation.class)
                        .eq(BizCrowdfundingDonation::getProjectId, id)
        );
        if (donationCount != null && donationCount > 0) {
            throw new BusinessException(400, "该项目已有捐款记录，禁止删除，请先禁用");
        }

        projectMapper.deleteById(id);
    }

    @Override
    @Transactional
    public CrowdfundingProjectDto create(Long userId, CreateCrowdfundingProjectRequest request) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        if (request == null) {
            throw new BusinessException(400, "请求不能为空");
        }

        String title = requireTrimmed(request.getTitle(), "title不能为空");
        Long coverFileId = request.getCoverFileId();
        validatePublicFile(coverFileId, "coverFileId不能为空");

        String content = requireTrimmed(request.getContent(), "content不能为空");
        validatePublicImagesInHtml(content);

        BigDecimal targetAmount = request.getTargetAmount();
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(400, "targetAmount必须大于0");
        }

        LocalDateTime endTime = request.getEndTime();
        if (endTime == null) {
            throw new BusinessException(400, "endTime不能为空");
        }

        BizCrowdfundingProject project = new BizCrowdfundingProject();
        project.setTitle(title);
        project.setCoverFileId(coverFileId);
        project.setContent(content);
        project.setTargetAmount(targetAmount);
        project.setRaisedAmount(BigDecimal.ZERO);
        project.setStartTime(LocalDateTime.now().withNano(0));
        project.setEndTime(endTime.withNano(0));
        project.setStatus(STATUS_PENDING);
        project.setEnabled(1);
        project.setDeleted(0);
        project.setCreatedBy(userId);

        projectMapper.insert(project);

        BizCrowdfundingProject created = projectMapper.selectById(project.getId());
        return toDto(created);
    }

    @Override
    @Transactional
    public CrowdfundingProjectDto update(Long userId, Long id, UpdateCrowdfundingProjectRequest request) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        if (id == null) {
            throw new BusinessException(400, "id不能为空");
        }
        if (request == null) {
            throw new BusinessException(400, "请求不能为空");
        }

        BizCrowdfundingProject existing = projectMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "众筹项目不存在");
        }
        if (existing.getCreatedBy() == null || !existing.getCreatedBy().equals(userId)) {
            throw new BusinessException(403, "无权操作该众筹项目");
        }
        String status = trimToNull(existing.getStatus());
        if (STATUS_APPROVED.equalsIgnoreCase(status)) {
            throw new BusinessException(400, "项目已通过审核，禁止修改");
        }

        BizCrowdfundingProject update = new BizCrowdfundingProject();
        update.setId(id);

        if (request.getTitle() != null) {
            update.setTitle(requireTrimmed(request.getTitle(), "title不能为空"));
        }
        if (request.getCoverFileId() != null) {
            validatePublicFile(request.getCoverFileId(), "coverFileId不能为空");
            update.setCoverFileId(request.getCoverFileId());
        }
        if (request.getContent() != null) {
            String content = requireTrimmed(request.getContent(), "content不能为空");
            validatePublicImagesInHtml(content);
            update.setContent(content);
        }
        if (request.getTargetAmount() != null) {
            BigDecimal ta = request.getTargetAmount();
            if (ta.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(400, "targetAmount必须大于0");
            }
            update.setTargetAmount(ta);
        }
        if (request.getEndTime() != null) {
            update.setEndTime(request.getEndTime().withNano(0));
        }

        projectMapper.updateById(update);

        BizCrowdfundingProject updated = projectMapper.selectById(id);
        return toDto(updated);
    }

    @Override
    @Transactional
    public void submit(Long userId, Long id) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        if (id == null) {
            throw new BusinessException(400, "id不能为空");
        }

        BizCrowdfundingProject existing = projectMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "众筹项目不存在");
        }
        if (existing.getCreatedBy() == null || !existing.getCreatedBy().equals(userId)) {
            throw new BusinessException(403, "无权操作该众筹项目");
        }

        String status = trimToNull(existing.getStatus());
        if (STATUS_APPROVED.equalsIgnoreCase(status)) {
            throw new BusinessException(400, "项目已通过审核，不能重新提交");
        }

        BizCrowdfundingProject update = new BizCrowdfundingProject();
        update.setId(id);
        update.setStatus(STATUS_PENDING);
        projectMapper.updateById(update);
    }

    @Override
    @Transactional
    public void review(Long adminUserId, Long id, ReviewCrowdfundingProjectRequest request) {
        if (adminUserId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        if (id == null) {
            throw new BusinessException(400, "id不能为空");
        }
        if (request == null) {
            throw new BusinessException(400, "请求不能为空");
        }

        BizCrowdfundingProject existing = projectMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "众筹项目不存在");
        }

        String action = trimToNull(request.getAction());
        if (action == null) {
            throw new BusinessException(400, "action不能为空");
        }
        String a = action.trim().toUpperCase(Locale.ROOT);
        String status = switch (a) {
            case "APPROVE" -> STATUS_APPROVED;
            case "REJECT" -> STATUS_REJECTED;
            default -> null;
        };
        if (status == null) {
            throw new BusinessException(400, "action不合法");
        }

        BizCrowdfundingProject update = new BizCrowdfundingProject();
        update.setId(id);
        update.setStatus(status);
        projectMapper.updateById(update);
    }

    @Override
    @Transactional
    public CrowdfundingDonationDto donate(Long userId, Long projectId, CreateCrowdfundingDonationRequest request) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        if (projectId == null) {
            throw new BusinessException(400, "projectId不能为空");
        }
        if (request == null) {
            throw new BusinessException(400, "请求不能为空");
        }

        BizCrowdfundingProject project = projectMapper.selectById(projectId);
        if (project == null || normalizeEnabledFlag(project.getEnabled()) != 1) {
            throw new BusinessException(404, "众筹项目不存在");
        }

        String status = trimToNull(project.getStatus());
        if (!STATUS_APPROVED.equalsIgnoreCase(status)) {
            throw new BusinessException(400, "众筹项目未通过审核");
        }

        LocalDateTime endTime = project.getEndTime();
        if (endTime != null && LocalDateTime.now().isAfter(endTime)) {
            throw new BusinessException(400, "众筹已结束，无法捐款");
        }

        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(400, "amount必须大于0");
        }

        BizCrowdfundingDonation donation = new BizCrowdfundingDonation();
        donation.setProjectId(projectId);
        donation.setUserId(userId);
        donation.setAmount(amount);
        donation.setIsAnonymous(request.getAnonymous() != null && request.getAnonymous() ? 1 : 0);
        donation.setRemark(trimToNull(request.getRemark()));

        donationMapper.insert(donation);

        BigDecimal currentRaised = project.getRaisedAmount() == null ? BigDecimal.ZERO : project.getRaisedAmount();
        BizCrowdfundingProject update = new BizCrowdfundingProject();
        update.setId(projectId);
        update.setRaisedAmount(currentRaised.add(amount));
        projectMapper.updateById(update);

        CrowdfundingDonationDto dto = new CrowdfundingDonationDto();
        dto.setId(donation.getId());
        dto.setProjectId(projectId);
        dto.setAmount(amount);
        dto.setCreatedAt(donation.getCreatedAt());
        dto.setDonorName(donation.getIsAnonymous() != null && donation.getIsAnonymous() == 1
                ? "匿名"
                : resolveDonorName(userId, new HashMap<>()));
        return dto;
    }

    @Override
    public PageResult<CrowdfundingDonationRecordDto> listMyDonations(Long userId, long current, long size) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        Page<BizCrowdfundingDonation> page = donationMapper.selectPage(
                new Page<>(current, size),
                Wrappers.lambdaQuery(BizCrowdfundingDonation.class)
                        .eq(BizCrowdfundingDonation::getUserId, userId)
                        .orderByDesc(BizCrowdfundingDonation::getCreatedAt)
                        .orderByDesc(BizCrowdfundingDonation::getId)
        );
        return toDonationRecordPageResult(page.getRecords(), page, true);
    }

    @Override
    public PageResult<CrowdfundingDonationRecordDto> listAllDonations(long current, long size, Long projectId) {
        Page<BizCrowdfundingDonation> page = donationMapper.selectPage(
                new Page<>(current, size),
                Wrappers.lambdaQuery(BizCrowdfundingDonation.class)
                        .eq(projectId != null, BizCrowdfundingDonation::getProjectId, projectId)
                        .orderByDesc(BizCrowdfundingDonation::getCreatedAt)
                        .orderByDesc(BizCrowdfundingDonation::getId)
        );
        return toDonationRecordPageResult(page.getRecords(), page, false);
    }

    private PageResult<CrowdfundingProjectDto> toPageResult(List<BizCrowdfundingProject> records, Page<?> page) {
        List<CrowdfundingProjectDto> dtos = records.stream().map(this::toDto).toList();
        PageResult<CrowdfundingProjectDto> result = new PageResult<>();
        result.setRecords(dtos);
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    private CrowdfundingProjectDto toDto(BizCrowdfundingProject project) {
        if (project == null) {
            return null;
        }
        CrowdfundingProjectDto dto = new CrowdfundingProjectDto();
        dto.setId(project.getId());
        dto.setTitle(project.getTitle());
        dto.setCoverFileId(project.getCoverFileId());
        dto.setTargetAmount(project.getTargetAmount());
        dto.setRaisedAmount(project.getRaisedAmount());
        dto.setStartTime(project.getStartTime());
        dto.setEndTime(project.getEndTime());
        dto.setStatus(project.getStatus());
        dto.setEnabled(project.getEnabled() == null ? 0 : project.getEnabled());
        dto.setCreatedBy(project.getCreatedBy());
        dto.setCreatedAt(project.getCreatedAt());
        return dto;
    }

    private CrowdfundingProjectDetailDto toDetailDto(BizCrowdfundingProject project) {
        if (project == null) {
            return null;
        }
        CrowdfundingProjectDetailDto dto = new CrowdfundingProjectDetailDto();
        dto.setId(project.getId());
        dto.setTitle(project.getTitle());
        dto.setCoverFileId(project.getCoverFileId());
        dto.setTargetAmount(project.getTargetAmount());
        dto.setRaisedAmount(project.getRaisedAmount());
        dto.setStartTime(project.getStartTime());
        dto.setEndTime(project.getEndTime());
        dto.setStatus(project.getStatus());
        dto.setEnabled(project.getEnabled() == null ? 0 : project.getEnabled());
        dto.setCreatedBy(project.getCreatedBy());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setContent(project.getContent());
        return dto;
    }

    private PageResult<CrowdfundingDonationRecordDto> toDonationRecordPageResult(
            List<BizCrowdfundingDonation> records,
            Page<?> page,
            boolean forMy
    ) {
        Map<Long, BizCrowdfundingProject> projects = loadProjects(
                records.stream().map(BizCrowdfundingDonation::getProjectId).toList()
        );
        Map<Long, SysUser> users = forMy
                ? Map.of()
                : loadUsers(records.stream().map(BizCrowdfundingDonation::getUserId).toList());

        List<CrowdfundingDonationRecordDto> dtos = records.stream()
                .map(r -> toDonationRecordDto(r, projects.get(r.getProjectId()), users.get(r.getUserId())))
                .toList();

        PageResult<CrowdfundingDonationRecordDto> result = new PageResult<>();
        result.setRecords(dtos);
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    private CrowdfundingDonationRecordDto toDonationRecordDto(
            BizCrowdfundingDonation donation,
            BizCrowdfundingProject project,
            SysUser user
    ) {
        if (donation == null) {
            return null;
        }
        CrowdfundingDonationRecordDto dto = new CrowdfundingDonationRecordDto();
        dto.setId(donation.getId());
        dto.setProjectId(donation.getProjectId());
        dto.setUserId(donation.getUserId());
        dto.setAmount(donation.getAmount());
        dto.setIsAnonymous(donation.getIsAnonymous());
        dto.setRemark(donation.getRemark());
        dto.setCreatedAt(donation.getCreatedAt());
        if (project != null) {
            dto.setProjectTitle(project.getTitle());
            dto.setProjectCoverFileId(project.getCoverFileId());
        }
        if (user != null) {
            dto.setUsername(user.getUsername());
        }
        return dto;
    }

    private CrowdfundingDonationDto toDonationDto(BizCrowdfundingDonation donation, Map<Long, SysUser> userCache) {
        if (donation == null) {
            return null;
        }
        CrowdfundingDonationDto dto = new CrowdfundingDonationDto();
        dto.setId(donation.getId());
        dto.setProjectId(donation.getProjectId());
        dto.setAmount(donation.getAmount());
        dto.setCreatedAt(donation.getCreatedAt());

        if (donation.getIsAnonymous() != null && donation.getIsAnonymous() == 1) {
            dto.setDonorName("匿名");
        } else {
            dto.setDonorName(resolveDonorName(donation.getUserId(), userCache));
        }
        return dto;
    }

    private Map<Long, BizCrowdfundingProject> loadProjects(List<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = projectIds.stream().distinct().toList();
        List<BizCrowdfundingProject> list = projectMapper.selectBatchIds(ids);
        Map<Long, BizCrowdfundingProject> map = new HashMap<>();
        for (BizCrowdfundingProject p : list) {
            map.put(p.getId(), p);
        }
        return map;
    }

    private Map<Long, SysUser> loadUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = userIds.stream().distinct().toList();
        List<SysUser> list = userMapper.selectBatchIds(ids);
        Map<Long, SysUser> map = new HashMap<>();
        for (SysUser u : list) {
            map.put(u.getId(), u);
        }
        return map;
    }

    private String resolveDonorName(Long userId, Map<Long, SysUser> cache) {
        if (userId == null) {
            return "用户";
        }
        if (cache != null && cache.containsKey(userId)) {
            SysUser cached = cache.get(userId);
            return pickDisplayName(cached);
        }
        SysUser user = userMapper.selectById(userId);
        if (cache != null) {
            cache.put(userId, user);
        }
        return pickDisplayName(user);
    }

    private String pickDisplayName(SysUser user) {
        if (user == null) {
            return "用户";
        }
        String nickname = trimToNull(user.getNickname());
        if (nickname != null) {
            return nickname;
        }
        String username = trimToNull(user.getUsername());
        return username == null ? "用户" : username;
    }

    private void validatePublicFile(Long fileId, String msgWhenNull) {
        if (fileId == null) {
            throw new BusinessException(400, msgWhenNull);
        }
        SysFile file = fileMapper.selectById(fileId);
        if (file == null || (file.getDeleted() != null && file.getDeleted() == 1)) {
            throw new BusinessException(1008, "文件不存在");
        }
        if (!"PUBLIC".equalsIgnoreCase(file.getVisibility())) {
            throw new BusinessException(400, "图片必须为PUBLIC");
        }
    }

    private void validatePublicImagesInHtml(String html) {
        if (html == null || html.isBlank()) {
            return;
        }
        Matcher matcher = FILE_DOWNLOAD_ID_PATTERN.matcher(html);
        Set<Long> ids = new HashSet<>();
        while (matcher.find()) {
            try {
                ids.add(Long.parseLong(matcher.group(1)));
            } catch (Exception ignored) {
                // ignore invalid id segments
            }
        }
        for (Long id : ids) {
            validatePublicFile(id, "图片不能为空");
        }
    }

    private Integer normalizeEnabledFlag(Integer enabled) {
        if (enabled == null) {
            return 1;
        }
        return enabled == 0 ? 0 : 1;
    }

    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String requireTrimmed(String s, String msg) {
        String t = trimToNull(s);
        if (t == null) {
            throw new BusinessException(400, msg);
        }
        return t;
    }
}
