package com.tiocantinas.senac.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tiocantinas.senac.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCpf(String cpf);
}
