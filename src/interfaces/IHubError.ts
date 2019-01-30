/**
 * Defines the shape of an error returned by an Identity Hub.
 */
export default interface IHubError {

  /** A standardized error code for determining the cause of the error. */
  error_code: string;

  /** The property in the request that caused the error, if any. */
  target?: string;

  /** A developer-understandable description of the error. */
  developer_message?: string;

  /** A user-understandable description of the error. */
  user_message?: string;

  /** The ISO datetime at which this error occurred. */
  timestamp?: string;

  /** The root cause error, if any. */
  inner_error?: {

    /** The ISO datetime at which this error occurred. */
    timestamp?: string;

    /** The stack trace of the error. */
    stacktrace?: string;
  };

}
