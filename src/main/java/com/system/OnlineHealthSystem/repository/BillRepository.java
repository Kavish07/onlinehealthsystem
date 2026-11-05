package com.system.OnlineHealthSystem.repository;

import com.system.OnlineHealthSystem.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<Bill, Integer> {
}
