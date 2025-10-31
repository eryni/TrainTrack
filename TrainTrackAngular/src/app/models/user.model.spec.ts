import { User } from './user.model';

describe('User', () => {
  it('should allow creation of a valid User object', () => {
    const user: User = {
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'User'
    };
    expect(user).toBeTruthy();
    expect(user.email).toBe('test@example.com');
  });
});