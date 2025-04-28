import { AbstractEntity } from './abstract-dto.model';

export interface UserDTO extends AbstractEntity {
  username?: string;
  firstName?: string;
  lastName?: string;
  position?: string;
  email?: string;
  available?: boolean;
}
