package com.sber.java13.ecoteam.service;

import com.sber.java13.ecoteam.constants.Errors;
import com.sber.java13.ecoteam.dto.OrderDTO;
import com.sber.java13.ecoteam.dto.PointDTO;
import com.sber.java13.ecoteam.dto.WasteDTO;
import com.sber.java13.ecoteam.exception.MyDeleteException;
import com.sber.java13.ecoteam.mapper.OrderMapper;
import com.sber.java13.ecoteam.model.Order;
import com.sber.java13.ecoteam.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class OrderService extends GenericService<Order, OrderDTO> {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final WasteService wasteService;
    private final PointService pointService;
    private final UserService userService;
    
    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper, WasteService wasteService, PointService pointService, UserService userService) {
        super(orderRepository, orderMapper);
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.wasteService = wasteService;
        this.pointService = pointService;
        this.userService = userService;
    }
    
    public Page<OrderDTO> listUserOrders(final Long id, final Pageable pageable) {
        Page<Order> objects = orderRepository.getOrderByUserId(id, pageable);
        List<OrderDTO> results = orderMapper.toDTOs(objects.getContent());
        return new PageImpl<>(results, pageable, objects.getTotalElements());
    }
    
    public Page<OrderDTO> listAgentOrders(final Long id, final Pageable pageable) {
        Long pointId = userService.getOne(id).getPointId();
        Page<Order> objects = orderRepository.getOrderByPointId(pointId, pageable);
        List<OrderDTO> results = orderMapper.toDTOs(objects.getContent());
        return new PageImpl<>(results, pageable, objects.getTotalElements());
    }
    
    public OrderDTO orderWaste(OrderDTO orderDTO) {
        WasteDTO wasteDTO = wasteService.getOne(orderDTO.getWasteId());
        wasteDTO.getOrdersIds().add(orderDTO.getWasteId());
        wasteService.update(wasteDTO);
        PointDTO pointDTO = pointService.getOne(orderDTO.getPointId());
        pointDTO.getOrdersIds().add(orderDTO.getPointId());
        pointService.update(pointDTO);
        orderDTO.setIsInWork(false);
        orderDTO.setIsCompleted(false);
        orderDTO.setCreatedWhen(LocalDateTime.now());
        orderDTO.setCreatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        return mapper.toDTO(repository.save(mapper.toEntity(orderDTO)));
    }
    
    public OrderDTO create(final OrderDTO orderDTO) {
        orderDTO.setCreatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        orderDTO.setCreatedWhen(LocalDateTime.now());
        return mapper.toDTO(orderRepository.save(mapper.toEntity(orderDTO)));
    }
    
    public OrderDTO update(final OrderDTO orderDTO) {
        return mapper.toDTO(orderRepository.save(mapper.toEntity(orderDTO)));
    }
    
    public void delete(Long id) throws MyDeleteException {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Заказа с заданным id=" + id + " не существует!"));
        boolean orderCanBeDeleted = orderRepository.checkOrderForDeletion(id);
        if (orderCanBeDeleted) {
            markAsDeleted(order);
            orderRepository.save(order);
        }
        else {
            throw new MyDeleteException(Errors.Orders.ORDER_DELETE_ERROR);
        }
    }
    
    public void restore(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Заказа с заданным id=\" + id + \" не существует!"));
        unMarkAsDeleted(order);
        orderRepository.save(order);
    }
    
    public void cancelOrder(final Long id) {
        OrderDTO orderDTO = getOne(id);
        orderDTO.setIsInWork(false);
        orderDTO.setIsCompleted(true);
        update(orderDTO);
    }
    
    public void acceptOrder(final Long id) {
        OrderDTO orderDTO = getOne(id);
        orderDTO.setIsInWork(true);
        orderDTO.setIsCompleted(false);
        update(orderDTO);
    }
    
    public void completeOrder(final Long id) {
        OrderDTO orderDTO = getOne(id);
        orderDTO.setIsInWork(false);
        orderDTO.setIsCompleted(true);
        update(orderDTO);
    }
}
