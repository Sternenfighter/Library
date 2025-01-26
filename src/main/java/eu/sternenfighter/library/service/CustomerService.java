package eu.sternenfighter.library.service;

import eu.sternenfighter.library.model.Customer;
import eu.sternenfighter.library.repository.CustomerRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer saveCustomer(Customer income) {
        if (validateEmail(income.getEmail())) {
            Customer customer = new Customer();
            customer.setName(income.getName());
            customer.setEmail(income.getEmail());
            customer.setPassword(DigestUtils.sha256Hex(income.getPassword()));
            return customerRepository.save(customer);
        }
        throw new IllegalArgumentException("Something went wrong");
    }

    public Optional<Customer> findById(long id) {
        return customerRepository.findById(id);
    }

    public Customer updateCustomer(long id, Customer customer) {
        if (validateEmail(customer.getEmail())) {
            Optional<Customer> optional = customerRepository.findById(id);
            if (optional.isPresent()) {
                customer.setId(optional.get().getId());
                return customerRepository.save(customer);
            }
        }
        throw new IllegalArgumentException("Something went wrong");
    }

    public void deleteCustomer(long id) {
        customerRepository.deleteById(id);
    }

    public Optional<Customer> login(String email, String password) {
        if (validateEmail(email)) {
            Optional<Customer> optional = customerRepository.findByEmail(email);
            if (optional.isPresent() && optional.get().getPassword().equals(DigestUtils.sha256Hex(password))) {
                    return optional;
            }
            return Optional.empty();
        }
        throw new UsernameNotFoundException("User not Found");
    }

    private boolean validateEmail(String mail) {
        return mail.matches("([a-zA-Z0-9])+@([a-zA-Z0-9])+\\.([a-zA-Z0-9])+");
    }
}
