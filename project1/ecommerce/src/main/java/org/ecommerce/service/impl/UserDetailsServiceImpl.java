package org.ecommerce.service.impl;

import org.ecommerce.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService
{
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException
    {
        return userRepository.findByEmail(email).map(CustomUserDetails::new) // Wraps the entity automatically if found
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
