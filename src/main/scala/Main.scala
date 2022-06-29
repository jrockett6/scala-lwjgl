package wafflepop

import cats.effect.{IO, IOApp}
import java.nio.*
import org.lwjgl.*
import org.lwjgl.glfw.*
import org.lwjgl.glfw.Callbacks.*
import org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.Configuration.DEBUG
import org.lwjgl.system.MemoryStack.*
import org.lwjgl.system.MemoryUtil.*
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.EXTDebugUtils.*
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkInstance
import scala.util.Using
import cats.MonadError

@main
def main = {
  val window         = Init.initWindow
  val instance       = Init.createVkInstance
  val debugMessenger = ValidationLayers.setupDebugMessenger(instance)
  Loop.loop(window)
  Cleanup.cleanup(window, instance, debugMessenger)
}

object Init {
  def initWindow: Long = {
    val status = glfwInit()
    if (!status) throw new Throwable("Unable to initialize GLFW")

    glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)

    val window = glfwCreateWindow(500, 500, "Hello World!", NULL, NULL);
    if (window == NULL) throw new Throwable("Failed to create the GLFW window")

    window
  }

  def createVkInstance: VkInstance = {
    // Configuration.VULKAN_LIBRARY_NAME.set("/Users/jrockett/myworkspace/VulkanSDK/1.3.204.1/macOS/lib/libvulkan.dylib")
    if (ValidationLayers.ENABLE_VALIDATION_LAYERS && !ValidationLayers.checkValidationLayerSupport)
      throw new RuntimeException(s"Validation requested but not supported")

    Using(stackPush) { stack =>

      // Use calloc to initialize the structs with 0s. Otherwise, the program can crash due to random values
      val appInfo: VkApplicationInfo = VkApplicationInfo.callocStack(stack);

      appInfo.sType(VK_STRUCTURE_TYPE_APPLICATION_INFO);
      appInfo.pApplicationName(stack.UTF8("Hello Triangle"))
      appInfo.applicationVersion(VK_MAKE_VERSION(1, 3, 0))
      appInfo.pEngineName(stack.UTF8("No Engine"))
      appInfo.engineVersion(VK_MAKE_VERSION(1, 3, 0))
      appInfo.apiVersion(VK_MAKE_VERSION(1, 3, 0))

      val createInfo: VkInstanceCreateInfo = VkInstanceCreateInfo.callocStack(stack);

      createInfo.sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
      createInfo.pApplicationInfo(appInfo)
      // enabledExtensionCount is implicitly set when you call ppEnabledExtensionNames
      createInfo.ppEnabledExtensionNames(glfwGetRequiredInstanceExtensions())
      // same with enabledLayerCount
      createInfo.ppEnabledLayerNames(null)

      if (ValidationLayers.ENABLE_VALIDATION_LAYERS) {
        createInfo.ppEnabledLayerNames(ValidationLayers.validationLayersAsPointerBuffer)

        val debugCreateInfo: VkDebugUtilsMessengerCreateInfoEXT =
          VkDebugUtilsMessengerCreateInfoEXT.callocStack(stack)
        ValidationLayers.populateDebugMessengerCreateInfo(debugCreateInfo);
        createInfo.pNext(debugCreateInfo.address());
      }

      // We need to retrieve the pointer of the created instance
      val instancePtr: PointerBuffer = stack.mallocPointer(1)
      val vkCode                     = vkCreateInstance(createInfo, null, instancePtr)

      if (vkCode != VK_SUCCESS) {
        throw new RuntimeException(s"Failed to create instance, error: $vkCode")
      }

      new VkInstance(instancePtr.get(0), createInfo)
    }.get
  }
}

object Loop {
  def loop(window: Long): Unit = {
    while (!glfwWindowShouldClose(window)) {
      glfwPollEvents
    }
  }
}

object Cleanup {
  def cleanup(window: Long, instance: VkInstance, debugMessenger: Long): Unit = {
    if (ValidationLayers.ENABLE_VALIDATION_LAYERS)
      ValidationLayers.destroyDebugUtilsMessengerEXT(instance, debugMessenger, null)
    vkDestroyInstance(instance, null)
    glfwDestroyWindow(window)
    glfwTerminate
  }
}

// object Init {
//   def init: IO[Long] =
//     for {
//       _      <- IO(glfwInit()).ifM(IO.unit, IO.raiseError(new Throwable("Unable to initialize GLFW")))
//       window <- IO(glfwCreateWindow(300, 300, "Hello World!", NULL, NULL))
//       _      <- IO.pure(window == NULL)
//                   .ifM(IO.unit, IO.raiseError(new Throwable("Failed to create the GLFW window")))
//       _      <- IO(glfwMakeContextCurrent(window))
//       _      <- IO(glfwSwapInterval(1))
//       _      <- IO(glfwShowWindow(window))
//     } yield (window)
// }

// object Loop {
//   def init: IO[Unit] =
//     for {
//       _ <- IO(GL.createCapabilities)
//       _ <- IO(glClearColor(1.0f, 0.0f, 0.0f, 0.0f))
//     } yield ()

//   def loop(window: Long): IO[Unit] =
//     for {
//       _ <- IO(glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT))
//       _ <- IO(glfwSwapBuffers(window))
//       _ <- IO(glfwPollEvents())
//     } yield ()

//   def go(window: Long): IO[Unit] = init *> loop(window).whileM_(IO(!glfwWindowShouldClose(window)))
// }
