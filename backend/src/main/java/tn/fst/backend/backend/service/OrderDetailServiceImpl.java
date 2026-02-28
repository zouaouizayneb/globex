package tn.fst.backend.backend.service;

import tn.fst.backend.backend.entity.OrderDetail;
import tn.fst.backend.backend.entity.Order;
import tn.fst.backend.backend.entity.Product;
import tn.fst.backend.backend.repository.OrderDetailRepository;
import tn.fst.backend.backend.repository.OrderRepository;
import tn.fst.backend.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderDetailServiceImpl implements OrderDetailService {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<OrderDetail> getAllOrderDetails() {
        return orderDetailRepository.findAll();
    }

    @Override
    public Optional<OrderDetail> getOrderDetailById(Long id) {
        return orderDetailRepository.findById(id);
    }

    @Override
    public OrderDetail createOrderDetail(OrderDetail orderDetail) {
        if (orderDetail.getOrder() != null) {
            orderRepository.findById(orderDetail.getOrder().getIdOrder())
                    .ifPresent(orderDetail::setOrder);
        }
        if (orderDetail.getProduct() != null) {
            productRepository.findById(orderDetail.getProduct().getIdProduct())
                    .ifPresent(orderDetail::setProduct);
        }
        return orderDetailRepository.save(orderDetail);
    }

    @Override
    public OrderDetail updateOrderDetail(Long id, OrderDetail orderDetailDetails) {
        Optional<OrderDetail> optional = orderDetailRepository.findById(id);
        if (!optional.isPresent()) throw new RuntimeException("OrderDetail not found with id: " + id);

        OrderDetail od = optional.get();
        od.setQuantity(orderDetailDetails.getQuantity());
        od.setPrice(orderDetailDetails.getPrice());

        if (orderDetailDetails.getOrder() != null)
            orderRepository.findById(orderDetailDetails.getOrder().getIdOrder())
                    .ifPresent(od::setOrder);

        if (orderDetailDetails.getProduct() != null)
            productRepository.findById(orderDetailDetails.getProduct().getIdProduct())
                    .ifPresent(od::setProduct);

        return orderDetailRepository.save(od);
    }

    @Override
    public void deleteOrderDetail(Long id) {
        if (!orderDetailRepository.existsById(id))
            throw new RuntimeException("OrderDetail not found with id: " + id);
        orderDetailRepository.deleteById(id);
    }
}

